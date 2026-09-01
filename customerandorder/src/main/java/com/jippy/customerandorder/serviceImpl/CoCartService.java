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

        log.info("SERVICE_START | SAVE_OR_UPDATE_CART | customerId={} | outletId={} | productId={} | variantOptionId={} | quantity={}", dto != null ? dto.getCustomerId() : null, dto != null ? dto.getOutletId() : null, dto != null ? dto.getProductId() : null, dto != null ? dto.getVariantOptionId() : null, dto != null ? dto.getQuantity() : null);

        validateSaveOrUpdateCartRequest(dto);

        try {

            /*
             * Quantity = 0 means remove the exact cart item.
             *
             * Cart item identity:
             *
             * customerId
             * +
             * productId
             * +
             * variantOptionId
             */
            if (dto.getQuantity() == 0) {

                CoCustomerCart existingCart = cartRepository.findByCustomerIdAndProductIdAndVariantOptionId(dto.getCustomerId(), dto.getProductId(), dto.getVariantOptionId()).orElse(null);

                if (existingCart == null) {

                    log.error("CART_ITEM_NOT_FOUND | customerId={} | outletId={} | productId={} | variantOptionId={}", dto.getCustomerId(), dto.getOutletId(), dto.getProductId(), dto.getVariantOptionId());

                    throw new CartException("Cart item not found");
                }

                validateCartOutlet(existingCart, dto);

                return removeCartItem(existingCart, dto);
            }

            /*
             * GET CUSTOMER EXISTING CART
             */
            List<CoCustomerCart> existingCartList = cartRepository.findByCustomerId(dto.getCustomerId());

            /*
             * CUSTOMER CAN HAVE ITEMS FROM ONLY ONE OUTLET.
             */
            if (!existingCartList.isEmpty()) {

                Integer existingOutletId = existingCartList.get(0).getOutletId();

                if (existingOutletId == null) {

                    log.error("CART_OUTLET_ID_NULL | customerId={} | cartId={}", dto.getCustomerId(), existingCartList.get(0).getCartId());

                    throw new CartException("Existing cart outlet information not found");
                }

                /*
                 * NEW PRODUCT FROM DIFFERENT OUTLET
                 *
                 * Clear the complete existing cart.
                 */
                if (!existingOutletId.equals(dto.getOutletId())) {

                    log.info("CART_OUTLET_CHANGED | customerId={} | oldOutletId={} | newOutletId={}", dto.getCustomerId(), existingOutletId, dto.getOutletId());

                    cartRepository.deleteByCustomerId(dto.getCustomerId());

                    log.info("OLD_CART_CLEARED | customerId={} | oldOutletId={} | newOutletId={}", dto.getCustomerId(), existingOutletId, dto.getOutletId());
                }
            }

            /*
             * FIND EXACT PRODUCT + VARIANT.
             *
             * Normal product:
             *
             * productId = 100
             * variantOptionId = NULL
             *
             * Variant product:
             *
             * productId = 100
             * variantOptionId = 20
             */
            CoCustomerCart existingCart = cartRepository.findByCustomerIdAndProductIdAndVariantOptionId(dto.getCustomerId(), dto.getProductId(), dto.getVariantOptionId()).orElse(null);

            BigDecimal totalPrice = calculateTotalPrice(dto.getUnitPrice(), dto.getQuantity());

            /*
             * UPDATE EXACT PRODUCT + VARIANT
             */
            if (existingCart != null) {

                existingCart.setQuantity(dto.getQuantity());

                existingCart.setTotalPrice(totalPrice);

                existingCart.setVariantOptionId(dto.getVariantOptionId());

                existingCart.setUpdatedAt(LocalDateTime.now());

                existingCart.setUpdatedBy(1);

                cartRepository.save(existingCart);

                log.info("CART_UPDATED | cartId={} | customerId={} | outletId={} | productId={} | variantOptionId={} | quantity={} | totalPrice={}", existingCart.getCartId(), dto.getCustomerId(), existingCart.getOutletId(), dto.getProductId(), dto.getVariantOptionId(), dto.getQuantity(), totalPrice);

                log.info("SERVICE_END | SAVE_OR_UPDATE_CART_SUCCESS | operation=UPDATE | customerId={}", dto.getCustomerId());

                return COConstants.MSG_CART_UPDATED;
            }

            /*
             * CREATE NEW CART ITEM
             */
            CoCustomerCart newCart = new CoCustomerCart();

            newCart.setCustomerId(dto.getCustomerId());

            newCart.setOutletId(dto.getOutletId());

            newCart.setProductId(dto.getProductId());

            /*
             * IMPORTANT:
             * Store selected variant option.
             */
            newCart.setVariantOptionId(dto.getVariantOptionId());

            newCart.setQuantity(dto.getQuantity());

            newCart.setTotalPrice(totalPrice);

            newCart.setCreatedAt(LocalDateTime.now());

            newCart.setCreatedBy(1);

            cartRepository.save(newCart);

            log.info("CART_CREATED | customerId={} | outletId={} | productId={} | variantOptionId={} | quantity={} | totalPrice={}", dto.getCustomerId(), dto.getOutletId(), dto.getProductId(), dto.getVariantOptionId(), dto.getQuantity(), totalPrice);

            log.info("SERVICE_END | SAVE_OR_UPDATE_CART_SUCCESS | operation=CREATE | customerId={}", dto.getCustomerId());

            return COConstants.MSG_CART_ADDED;

        } catch (CartException ex) {

            log.error("BUSINESS_EXCEPTION | SAVE_OR_UPDATE_CART_FAILED | customerId={} | outletId={} | productId={} | variantOptionId={} | error={}", dto.getCustomerId(), dto.getOutletId(), dto.getProductId(), dto.getVariantOptionId(), ex.getMessage(), ex);

            throw ex;

        } catch (Exception ex) {

            log.error("EXCEPTION | SAVE_OR_UPDATE_CART_FAILED | customerId={} | outletId={} | productId={} | variantOptionId={} | error={}", dto.getCustomerId(), dto.getOutletId(), dto.getProductId(), dto.getVariantOptionId(), ex.getMessage(), ex);

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

            Integer outletId = cartList.get(0).getOutletId();

            if (outletId == null) {

                log.error("CART_OUTLET_ID_NULL | customerId={} | cartId={}", customerId, cartList.get(0).getCartId());

                throw new CartException("Cart outlet information not found");
            }

            /*
             * Defensive validation.
             *
             * All cart items must belong
             * to the same outlet.
             */
            for (CoCustomerCart cart : cartList) {

                if (cart.getOutletId() == null) {

                    log.error("CART_ITEM_OUTLET_ID_NULL | customerId={} | cartId={} | productId={} | variantOptionId={}", customerId, cart.getCartId(), cart.getProductId(), cart.getVariantOptionId());

                    throw new CartException("Cart item outlet information not found");
                }

                if (!outletId.equals(cart.getOutletId())) {

                    log.error("MULTIPLE_OUTLETS_IN_CART | customerId={} | expectedOutletId={} | actualOutletId={} | productId={} | variantOptionId={}", customerId, outletId, cart.getOutletId(), cart.getProductId(), cart.getVariantOptionId());

                    throw new CartException("Cart contains items from multiple outlets");
                }
            }

            List<CoCartItemResponseDto> items = new ArrayList<>();

            BigDecimal grandTotal = BigDecimal.ZERO;

            for (CoCustomerCart cart : cartList) {

                CoProductDetailResponseDto product = getProduct(cart.getProductId());

                CoCartItemResponseDto item = new CoCartItemResponseDto();

                item.setProductId(cart.getProductId());

                /*
                 * Return selected variant.
                 */
                item.setVariantOptionId(cart.getVariantOptionId());
                item.setProductImage(product.getImageLink());

                item.setProductName(product.getProductName());

                item.setQuantity(cart.getQuantity());

                item.setTotalPrice(cart.getTotalPrice());

                items.add(item);

                grandTotal = grandTotal.add(defaultValue(cart.getTotalPrice()));
            }

            CoCartResponseDto response = new CoCartResponseDto();

            response.setCustomerId(customerId);

            response.setOutletId(outletId);

            response.setItems(items);

            response.setGrandTotal(grandTotal);

            log.info("SERVICE_END | GET_CART_SUCCESS | customerId={} | outletId={} | itemCount={} | grandTotal={}", customerId, outletId, items.size(), grandTotal);

            return response;

        } catch (CartException ex) {

            log.error("BUSINESS_EXCEPTION | GET_CART_FAILED | customerId={} | error={}", customerId, ex.getMessage(), ex);

            throw ex;

        } catch (Exception ex) {

            log.error("EXCEPTION | GET_CART_FAILED | customerId={} | error={}", customerId, ex.getMessage(), ex);

            throw new CartException("Unable to fetch cart");
        }
    }

    // ================= REMOVE CART =================

    private String removeCartItem(CoCustomerCart existingCart, CoCartUpdateRequestDto dto) {

        if (existingCart == null) {

            log.error("CART_ITEM_NOT_FOUND | customerId={} | productId={} | variantOptionId={}", dto.getCustomerId(), dto.getProductId(), dto.getVariantOptionId());

            throw new CartException("Cart item not found");
        }

        cartRepository.delete(existingCart);

        log.info("CART_REMOVED | cartId={} | customerId={} | productId={} | variantOptionId={}", existingCart.getCartId(), dto.getCustomerId(), existingCart.getProductId(), existingCart.getVariantOptionId());

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

        if (dto.getOutletId() == null || dto.getOutletId() <= 0) {

            log.error("VALIDATION_FAILED | INVALID_OUTLET_ID | outletId={}", dto.getOutletId());

            throw new CartException("Invalid outlet ID");
        }

        if (dto.getProductId() == null || dto.getProductId() <= 0) {

            log.error("VALIDATION_FAILED | INVALID_PRODUCT_ID | productId={}", dto.getProductId());

            throw new CartException("Invalid product ID");
        }

        /*
         * Variant is optional.
         *
         * NULL = product without variant.
         *
         * Positive value = selected variant option.
         */
        if (dto.getVariantOptionId() != null && dto.getVariantOptionId() <= 0) {

            log.error("VALIDATION_FAILED | INVALID_VARIANT_OPTION_ID | variantOptionId={}", dto.getVariantOptionId());

            throw new CartException("Invalid variant option ID");
        }

        if (dto.getQuantity() == null || dto.getQuantity() < 0) {

            log.error("VALIDATION_FAILED | INVALID_QUANTITY | quantity={}", dto.getQuantity());

            throw new CartException(COConstants.MSG_INVALID_QUANTITY);
        }

        /*
         * Unit price is required only when
         * adding/updating.
         */
        if (dto.getQuantity() > 0 && (dto.getUnitPrice() == null || dto.getUnitPrice().compareTo(BigDecimal.ZERO) < 0)) {

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

    private void validateCartOutlet(CoCustomerCart existingCart, CoCartUpdateRequestDto dto) {

        if (existingCart.getOutletId() == null) {

            log.error("CART_OUTLET_ID_NULL | customerId={} | cartId={}", dto.getCustomerId(), existingCart.getCartId());

            throw new CartException("Cart outlet information not found");
        }

        if (!existingCart.getOutletId().equals(dto.getOutletId())) {

            log.error("CART_OUTLET_MISMATCH | customerId={} | cartOutletId={} | requestedOutletId={} | productId={} | variantOptionId={}", dto.getCustomerId(), existingCart.getOutletId(), dto.getOutletId(), dto.getProductId(), dto.getVariantOptionId());

            throw new CartException("Selected outlet does not match the cart item outlet");
        }
    }

    // ================= PRODUCT HELPERS =================

    // ================= PRODUCT HELPERS =================

    private CoProductDetailResponseDto getProduct(Integer productId) {

        log.info("PRODUCT_FETCH_START | productId={}", productId);

        try {

            CoProductDetailResponseDto product = fmFeignClient.getProductDetailById(productId);

            if (product == null) {

                log.error("PRODUCT_NOT_FOUND | productId={}", productId);

                throw new CartException("Product not found with id: " + productId);
            }

            log.info("PRODUCT_FETCH_SUCCESS | productId={} | productName={} | hasVariants={}", product.getProductId(), product.getProductName(), product.getHasProductVariants());

            return product;

        } catch (CartException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error("FEIGN_EXCEPTION | PRODUCT_FETCH_FAILED | productId={} | error={}", productId, ex.getMessage(), ex);

            throw new CartException(COConstants.MSG_PRODUCT_FETCH_FAILED);
        }
    }

    // ================= COMMON METHODS =================

    private BigDecimal calculateTotalPrice(BigDecimal unitPrice, Integer quantity) {

        if (unitPrice == null) {

            throw new CartException("Unit price is required");
        }

        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private BigDecimal defaultValue(BigDecimal value) {

        return value == null ? BigDecimal.ZERO : value;
    }

    // ================= CART REMINDERS =================

    @Override
    @Transactional(readOnly = true)
    public List<CoCartReminderDto> getCartReminderCustomers() {

        log.info("SERVICE_START | GET_CART_REMINDERS");

        List<CoCartReminderProjection> reminders = cartRepository.findEligibleCartReminders();

        List<CoCartReminderDto> response = new ArrayList<>();

        for (CoCartReminderProjection cart : reminders) {

            String notificationSubject = cart.getCartTotal().compareTo(COConstants.HIGH_VALUE_CART_LIMIT) > 0 ? COConstants.HIGH_VALUE_CART : COConstants.ITEM_ADDED_NOT_ORDERED;

            CoCartReminderDto dto = new CoCartReminderDto();

            dto.setCustomerId(cart.getCustomerId());

            dto.setCartTotal(cart.getCartTotal());

            dto.setLastUpdated(cart.getLastUpdated());

            dto.setNotificationSubject(notificationSubject);

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

                log.info("Publishing Cart Reminder for Customer : {}", reminder.getCustomerId());

                cartReminderKafkaProducer.sendCartReminder(reminder);

            } catch (Exception ex) {

                log.error("Failed to publish cart reminder for customer {}", reminder.getCustomerId(), ex);
            }
        }

        log.info("SERVICE_END | PROCESS_CART_REMINDERS");
    }
}