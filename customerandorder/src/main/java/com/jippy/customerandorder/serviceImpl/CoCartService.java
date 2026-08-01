package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoCustomerCart;
import com.jippy.customerandorder.exception.CartException;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.ICartService;
import com.jippy.customerandorder.producer.CoCartReminderKafkaProducer;
import com.jippy.customerandorder.projection.CoCartReminderProjection;
import com.jippy.customerandorder.repository.CoCustomerCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CoCartService implements ICartService {

    private final CoCustomerCartRepository cartRepository;

    private final FMFeignClient fmFeignClient;

    private final CoCartReminderKafkaProducer cartReminderKafkaProducer;

    @Override
    public String saveOrUpdateCart(CoCartUpdateRequestDto dto) {

        log.info("SERVICE_START | SAVE_OR_UPDATE_CART | customerId={} | productId={} | quantity={}", dto.getCustomerId(), dto.getProductId(), dto.getQuantity());

        validateSaveOrUpdateCartRequest(dto);

        try {

            CoCustomerCart existingCart = cartRepository.findByCustomerIdAndProductId(dto.getCustomerId(), dto.getProductId()).orElse(null);

            // REMOVE CART ITEM
            if (dto.getQuantity() == 0) {

                return removeCartItem(existingCart, dto);
            }

            BigDecimal totalPrice = dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));

            // UPDATE CART
            if (existingCart != null) {

                existingCart.setQuantity(dto.getQuantity());

                existingCart.setTotalPrice(totalPrice);

                existingCart.setUpdatedAt(LocalDateTime.now());

                existingCart.setUpdatedBy(1);

                cartRepository.save(existingCart);

                log.info("CART_UPDATED | cartId={} | customerId={} | productId={} | quantity={}", existingCart.getCartId(), dto.getCustomerId(), dto.getProductId(), dto.getQuantity());

                log.info("SERVICE_END | SAVE_OR_UPDATE_CART_SUCCESS | operation=UPDATE | customerId={}", dto.getCustomerId());

                return COConstants.MSG_CART_UPDATED;
            }

            // CREATE CART ITEM
            CoCustomerCart newCart = new CoCustomerCart();

            newCart.setCustomerId(dto.getCustomerId());

            newCart.setProductId(dto.getProductId());

            newCart.setQuantity(dto.getQuantity());

            newCart.setTotalPrice(totalPrice);

            newCart.setCreatedAt(LocalDateTime.now());

            newCart.setCreatedBy(1);

            cartRepository.save(newCart);

            log.info("CART_CREATED | customerId={} | productId={} | quantity={}", dto.getCustomerId(), dto.getProductId(), dto.getQuantity());

            log.info("SERVICE_END | SAVE_OR_UPDATE_CART_SUCCESS | operation=CREATE | customerId={}", dto.getCustomerId());

            return COConstants.MSG_CART_ADDED;

        } catch (CartException ex) {

            log.error("BUSINESS_EXCEPTION | SAVE_OR_UPDATE_CART_FAILED | error={}", ex.getMessage(), ex);

            throw ex;

        } catch (Exception ex) {

            log.error("EXCEPTION | SAVE_OR_UPDATE_CART_FAILED | customerId={} | productId={} | error={}", dto.getCustomerId(), dto.getProductId(), ex.getMessage(), ex);

            throw new CartException("Unable to save or update cart");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CoCartResponseDto getCart(Integer customerId) {

        log.info("SERVICE_START | GET_CART | customerId={}", customerId);

        validateCustomerId(customerId);

        try {

            List<CoCustomerCart> cartList = cartRepository.findByCustomerId(customerId);

            if (cartList.isEmpty()) {

                log.error("CART_EMPTY | customerId={}", customerId);

                throw new CartException(COConstants.MSG_CART_EMPTY);
            }

            List<CoCartItemResponseDto> items = new ArrayList<>();

            BigDecimal grandTotal = BigDecimal.ZERO;

            for (CoCustomerCart cart : cartList) {

                FmProductDetailResponseDto product = getProduct(cart.getProductId());

                CoCartItemResponseDto item = new CoCartItemResponseDto();

                item.setProductId(cart.getProductId());

                item.setProductName(product.getProductName());

                item.setProductImage(product.getProductImage());

                item.setQuantity(cart.getQuantity());

                item.setTotalPrice(cart.getTotalPrice());

                items.add(item);

                grandTotal = grandTotal.add(defaultValue(cart.getTotalPrice()));
            }

            CoCartResponseDto response = new CoCartResponseDto();

            response.setCustomerId(customerId);

            response.setItems(items);

            response.setGrandTotal(grandTotal);

            log.info("SERVICE_END | GET_CART_SUCCESS | customerId={} | itemCount={} | grandTotal={}", customerId, items.size(), grandTotal);

            return response;

        } catch (CartException ex) {

            log.error("BUSINESS_EXCEPTION | GET_CART_FAILED | error={}", ex.getMessage(), ex);

            throw ex;

        } catch (Exception ex) {

            log.error("EXCEPTION | GET_CART_FAILED | customerId={} | error={}", customerId, ex.getMessage(), ex);

            throw new CartException("Unable to fetch cart");
        }
    }

    // ================= REMOVE CART =================

    private String removeCartItem(CoCustomerCart existingCart, CoCartUpdateRequestDto dto) {

        if (existingCart == null) {

            log.error("CART_ITEM_NOT_FOUND | customerId={} | productId={}", dto.getCustomerId(), dto.getProductId());

            throw new CartException("Cart item not found");
        }

        cartRepository.delete(existingCart);

        log.info("CART_REMOVED | cartId={} | customerId={} | productId={}", existingCart.getCartId(), dto.getCustomerId(), dto.getProductId());

        log.info("SERVICE_END | SAVE_OR_UPDATE_CART_SUCCESS | operation=REMOVE | customerId={}", dto.getCustomerId());

        return COConstants.MSG_CART_REMOVED;
    }

    // ================= VALIDATIONS =================

    private void validateSaveOrUpdateCartRequest(CoCartUpdateRequestDto dto) {

        if (dto == null) {

            log.error("VALIDATION_FAILED | REQUEST_BODY_NULL");

            throw new CartException("Request body cannot be null");
        }

        validateCustomerId(dto.getCustomerId());

        if (dto.getProductId() == null || dto.getProductId() <= 0) {

            log.error("VALIDATION_FAILED | INVALID_PRODUCT_ID | productId={}", dto.getProductId());

            throw new CartException("Invalid product ID");
        }

        if (dto.getQuantity() < 0) {

            log.error("VALIDATION_FAILED | INVALID_QUANTITY | quantity={}", dto.getQuantity());

            throw new CartException(COConstants.MSG_INVALID_QUANTITY);
        }

        if (dto.getUnitPrice() == null || dto.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {

            log.error("VALIDATION_FAILED | INVALID_UNIT_PRICE | unitPrice={}", dto.getUnitPrice());

            throw new CartException("Unit price cannot be negative");
        }
    }

    private void validateCustomerId(Integer customerId) {

        if (customerId == null || customerId <= 0) {

            log.error("VALIDATION_FAILED | INVALID_CUSTOMER_ID | customerId={}", customerId);

            throw new CartException("Invalid customer ID");
        }
    }

    // ================= PRODUCT HELPERS =================

    private FmProductDetailResponseDto getProduct(Integer productId) {

        try {

            FmProductDetailResponseDto product = fmFeignClient.getProductById(productId);

            if (product == null) {

                log.error("PRODUCT_NOT_FOUND | productId={}", productId);

                throw new CartException("Product not found");
            }

            return product;

        } catch (CartException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error("FEIGN_EXCEPTION | PRODUCT_FETCH_FAILED | productId={} | error={}", productId, ex.getMessage(), ex);

            throw new CartException(COConstants.MSG_PRODUCT_FETCH_FAILED);
        }
    }

    // ================= COMMON METHODS =================

    private BigDecimal defaultValue(BigDecimal value) {

        return value == null ? BigDecimal.ZERO : value;
    }
    @Override
    @Transactional(readOnly = true)
    public List<CoCartReminderDto> getCartReminderCustomers() {

        log.info("SERVICE_START | GET_CART_REMINDERS");

        List<CoCartReminderProjection> reminders =
                cartRepository.findEligibleCartReminders();

        List<CoCartReminderDto> response = new ArrayList<>();

        for (CoCartReminderProjection cart : reminders) {

            String notificationSubject =
                    cart.getCartTotal().compareTo(COConstants.HIGH_VALUE_CART_LIMIT) > 0
                            ? COConstants.HIGH_VALUE_CART
                            : COConstants.ITEM_ADDED_NOT_ORDERED;

            CoCartReminderDto dto = CoCartReminderDto.builder()
                    .customerId(cart.getCustomerId())
                    .cartTotal(cart.getCartTotal())
                    .lastUpdated(cart.getLastUpdated())
                    .notificationSubject(notificationSubject)
                    .build();

            response.add(dto);
        }

        log.info("SERVICE_END | GET_CART_REMINDERS | count={}", response.size());

        return response;
    }
    @Override
    public void processCartReminders() {

        log.info("SERVICE_START | PROCESS_CART_REMINDERS");

        List<CoCartReminderDto> reminders = getCartReminderCustomers();

        if (reminders == null || reminders.isEmpty()) {

            log.info("No abandoned carts found.");

            return;
        }

        log.info("Total abandoned carts : {}", reminders.size());

        for (CoCartReminderDto reminder : reminders) {

            try {

                log.info("Publishing Cart Reminder for Customer : {}",
                        reminder.getCustomerId());

                cartReminderKafkaProducer.sendCartReminder(reminder);

            } catch (Exception ex) {

                log.error("Failed to publish cart reminder for customer {}",
                        reminder.getCustomerId(), ex);
            }
        }

        log.info("SERVICE_END | PROCESS_CART_REMINDERS");
    }
}