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

        log.info("SERVICE_START | SAVE_OR_UPDATE_CART | customerId={} | outletId={} | productId={} | variantCount={}", dto != null ? dto.getCustomerId() : null, dto != null ? dto.getOutletId() : null, dto != null ? dto.getProductId() : null, dto != null && dto.getVariants() != null ? dto.getVariants().size() : 0);

        validateSaveOrUpdateCartRequest(dto);

        try {

            /*
             * 1. CHECK EXISTING CUSTOMER CART
             */
            List<CoCustomerCart> existingCartList = cartRepository.findByCustomerId(dto.getCustomerId());

            /*
             * 2. ONE OUTLET PER CUSTOMER CART
             */
            if (!existingCartList.isEmpty()) {

                Integer existingOutletId = existingCartList.get(0).getOutletId();

                if (existingOutletId == null) {

                    log.error("CART_OUTLET_ID_NULL | customerId={} | cartId={}", dto.getCustomerId(), existingCartList.get(0).getCartId());

                    throw new CartException("Existing cart outlet information not found");
                }

                /*
                 * If customer selects a different outlet,
                 * clear the existing cart.
                 */
                if (!existingOutletId.equals(dto.getOutletId())) {

                    log.info("CART_OUTLET_CHANGED | customerId={} | oldOutletId={} | newOutletId={}", dto.getCustomerId(), existingOutletId, dto.getOutletId());

                    cartRepository.deleteByCustomerId(dto.getCustomerId());

                    log.info("OLD_CART_CLEARED | customerId={} | oldOutletId={} | newOutletId={}", dto.getCustomerId(), existingOutletId, dto.getOutletId());
                }
            }

            /*
             * 3. PROCESS ALL VARIANTS
             */
            for (CoCartVariantDto variant : dto.getVariants()) {

                Integer variantOptionId = variant.getVariantOptionId();

                Integer quantity = variant.getQuantity();

                BigDecimal unitPrice = variant.getUnitPrice();

                log.info("PROCESS_CART_VARIANT | customerId={} | outletId={} | productId={} | variantOptionId={} | quantity={} | unitPrice={}", dto.getCustomerId(), dto.getOutletId(), dto.getProductId(), variantOptionId, quantity, unitPrice);

                //  * * 4. QUANTITY = 0 (REMOVE ITEM) * Remove ONLY this product + variant.

                if (quantity == 0) {

                    CoCustomerCart existingCart = cartRepository.findByCustomerIdAndProductIdAndVariantOptionId(dto.getCustomerId(), dto.getProductId(), variantOptionId).orElse(null);

                    if (existingCart == null) {

                        log.warn("CART_VARIANT_NOT_FOUND_FOR_REMOVE | customerId={} | productId={} | variantOptionId={}", dto.getCustomerId(), dto.getProductId(), variantOptionId);

                        continue;
                    }

                    validateCartOutlet(existingCart, dto.getOutletId());

                    cartRepository.delete(existingCart);

                    log.info("CART_VARIANT_REMOVED | cartId={} | customerId={} | productId={} | variantOptionId={}", existingCart.getCartId(), dto.getCustomerId(), dto.getProductId(), variantOptionId);

                    continue;
                }

                /*
                 * 5. FIND EXACT PRODUCT + VARIANT
                 */
                CoCustomerCart existingCart = cartRepository.findByCustomerIdAndProductIdAndVariantOptionId(dto.getCustomerId(), dto.getProductId(), variantOptionId).orElse(null);

                /*
                 * totalPrice = unitPrice × quantity
                 */
                BigDecimal totalPrice = calculateTotalPrice(unitPrice, quantity);

                /*
                 * ========================================================
                 * 7. UPDATE EXISTING VARIANT
                 * ========================================================
                 */
                if (existingCart != null) {

                    existingCart.setQuantity(quantity);

                    existingCart.setTotalPrice(totalPrice);

                    existingCart.setUpdatedAt(LocalDateTime.now());

                    existingCart.setUpdatedBy(1);

                    cartRepository.save(existingCart);

                    log.info("CART_VARIANT_UPDATED | cartId={} | customerId={} | outletId={} | productId={} | variantOptionId={} | quantity={} | totalPrice={}", existingCart.getCartId(), dto.getCustomerId(), existingCart.getOutletId(), dto.getProductId(), variantOptionId, quantity, totalPrice);

                } else {

                    /*
                     * ====================================================
                     * 8. CREATE NEW VARIANT
                     * ====================================================
                     */
                    CoCustomerCart newCart = new CoCustomerCart();

                    newCart.setCustomerId(dto.getCustomerId());

                    newCart.setOutletId(dto.getOutletId());

                    newCart.setProductId(dto.getProductId());

                    newCart.setVariantOptionId(variantOptionId);

                    newCart.setQuantity(quantity);

                    newCart.setTotalPrice(totalPrice);

                    newCart.setCreatedAt(LocalDateTime.now());

                    newCart.setCreatedBy(1);

                    cartRepository.save(newCart);

                    log.info("CART_VARIANT_CREATED | cartId={} | customerId={} | outletId={} | productId={} | variantOptionId={} | quantity={} | totalPrice={}", newCart.getCartId(), dto.getCustomerId(), dto.getOutletId(), dto.getProductId(), variantOptionId, quantity, totalPrice);
                }
            }

            log.info("SERVICE_END | SAVE_OR_UPDATE_CART_SUCCESS | customerId={} | outletId={} | productId={} | variantCount={}", dto.getCustomerId(), dto.getOutletId(), dto.getProductId(), dto.getVariants().size());

            return COConstants.MSG_CART_UPDATED;

        } catch (CartException ex) {

            log.error("BUSINESS_EXCEPTION | SAVE_OR_UPDATE_CART_FAILED | customerId={} | outletId={} | productId={} | error={}", dto.getCustomerId(), dto.getOutletId(), dto.getProductId(), ex.getMessage(), ex);

            throw ex;

        } catch (Exception ex) {

            log.error("EXCEPTION | SAVE_OR_UPDATE_CART_FAILED | customerId={} | outletId={} | productId={} | error={}", dto.getCustomerId(), dto.getOutletId(), dto.getProductId(), ex.getMessage(), ex);

            throw new CartException("Unable to save or update cart");
        }
    }

    @Override
    @Transactional
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
             * 1. VALIDATE ALL CART ITEMS BELONG TO SAME OUTLET
             */
            for (CoCustomerCart cart : cartList) {

                if (cart.getOutletId() == null) {

                    log.error("CART_ITEM_OUTLET_ID_NULL | customerId={} | cartId={}", customerId, cart.getCartId());

                    throw new CartException("Cart item outlet information not found");
                }

                if (!outletId.equals(cart.getOutletId())) {

                    log.error("MULTIPLE_OUTLETS_IN_CART | customerId={} | expectedOutletId={} | actualOutletId={} | productId={} | variantOptionId={}", customerId, outletId, cart.getOutletId(), cart.getProductId(), cart.getVariantOptionId());

                    throw new CartException("Cart contains items from multiple outlets");
                }
            }

            /*
             * 2. BUILD ONE FM BULK PRICE REQUEST
             */
            List<CoCurrentOnlinePriceItemRequestDto> priceItems = new ArrayList<>();

            for (CoCustomerCart cart : cartList) {

                CoCurrentOnlinePriceItemRequestDto priceItem = new CoCurrentOnlinePriceItemRequestDto();

                priceItem.setProductId(cart.getProductId());

                priceItem.setVariantOptionId(cart.getVariantOptionId());

                priceItems.add(priceItem);
            }

            CoCurrentOnlinePriceRequestDto priceRequest = new CoCurrentOnlinePriceRequestDto();

            priceRequest.setOutletId(outletId);
            priceRequest.setItems(priceItems);

            /*
             * 3. ONE FEIGN CALL TO FM
             */
            log.info("FM_PRICE_REQUEST_START | customerId={} | outletId={} | itemCount={}", customerId, outletId, priceItems.size());

            List<CoCurrentOnlinePriceResponseDto> currentPrices = fmFeignClient.getCurrentOnlinePrices(priceRequest);

            if (currentPrices == null) {

                log.error("FM_PRICE_RESPONSE_NULL | customerId={} | outletId={}", customerId, outletId);

                throw new CartException("Unable to fetch current product prices");
            }

            log.info("FM_PRICE_REQUEST_SUCCESS | customerId={} | outletId={} | responseCount={}", customerId, outletId, currentPrices.size());

            /*
             * 4. BUILD PRICE MAP
             *
             * Key:
             *
             * productId + variantOptionId
             *
             * Example:
             *
             * 18_38   -> ₹110
             * 18_NULL -> ₹95
             */
            java.util.Map<String, CoCurrentOnlinePriceResponseDto> currentPriceMap = new java.util.HashMap<>();

            for (CoCurrentOnlinePriceResponseDto price : currentPrices) {

                String key = buildPriceKey(price.getProductId(), price.getVariantOptionId());

                currentPriceMap.put(key, price);
            }

            /*
             * 5. BUILD CART RESPONSE
             *              */
            List<CoCartItemResponseDto> items = new ArrayList<>();

            BigDecimal grandTotal = BigDecimal.ZERO;

            for (CoCustomerCart cart : cartList) {

                String key = buildPriceKey(cart.getProductId(), cart.getVariantOptionId());

                CoCurrentOnlinePriceResponseDto latestPrice = currentPriceMap.get(key);

                if (latestPrice == null) {

                    log.error("CURRENT_PRICE_NOT_FOUND | customerId={} | outletId={} | productId={} | variantOptionId={}", customerId, outletId, cart.getProductId(), cart.getVariantOptionId());

                    throw new CartException("Current price not found for product: " + cart.getProductId());
                }

                /*
                 * ========================================================
                 * 6. AVAILABILITY CHECK
                 * ========================================================
                 */
                if (!Boolean.TRUE.equals(latestPrice.getAvailable())) {

                    log.warn("PRODUCT_UNAVAILABLE | customerId={} | productId={} | variantOptionId={}", customerId, cart.getProductId(), cart.getVariantOptionId());

                    throw new CartException("Product is currently unavailable: " + cart.getProductId());
                }

                BigDecimal latestUnitPrice = latestPrice.getOnlinePrice();

                if (latestUnitPrice == null) {

                    log.error("CURRENT_PRICE_NULL | productId={} | variantOptionId={}", cart.getProductId(), cart.getVariantOptionId());

                    throw new CartException("Current online price not found");
                }

                /*
                 * 7. COMPARE OLD PRICE VS CURRENT FM PRICE
                 */
                BigDecimal oldUnitPrice = calculateUnitPrice(cart.getTotalPrice(), cart.getQuantity());

                boolean priceChanged = oldUnitPrice.compareTo(latestUnitPrice) != 0;

                if (priceChanged) {

                    BigDecimal oldTotalPrice = defaultValue(cart.getTotalPrice());

                    BigDecimal newTotalPrice = calculateTotalPrice(latestUnitPrice, cart.getQuantity());

                    log.info("CART_PRICE_CHANGED | customerId={} | cartId={} | productId={} | variantOptionId={} | oldUnitPrice={} | newUnitPrice={} | oldTotal={} | newTotal={}", customerId, cart.getCartId(), cart.getProductId(), cart.getVariantOptionId(), oldUnitPrice, latestUnitPrice, oldTotalPrice, newTotalPrice);

                    /* 8. UPDATE CART WITH LATEST PRICE                     */
                    cart.setTotalPrice(newTotalPrice);

                    cart.setUpdatedAt(LocalDateTime.now());

                    cart.setUpdatedBy(1);

                    cartRepository.save(cart);
                }

                /*
                 * ========================================================
                 * 9. PRODUCT DETAILS
                 *
                 * IMPORTANT:
                 *
                 * Do NOT call old FM product API here.
                 *
                 * For now use the data available in the current-price
                 * response / existing cart response structure.
                 * ========================================================
                 */
                CoCartItemResponseDto item = new CoCartItemResponseDto();

                item.setProductId(cart.getProductId());

                item.setVariantOptionId(cart.getVariantOptionId());

                item.setProductName(latestPrice.getProductName());

                item.setProductImage(latestPrice.getProductImage());

                item.setQuantity(cart.getQuantity());

                item.setTotalPrice(calculateTotalPrice(latestUnitPrice, cart.getQuantity()));
                items.add(item);

                grandTotal = grandTotal.add(defaultValue(item.getTotalPrice()));
            }

            /*
             *              * 10. BUILD FINAL RESPONSE
             */
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

    private String buildPriceKey(Integer productId, Integer variantOptionId) {

        return productId + "_" + (variantOptionId == null ? "NULL" : variantOptionId);
    }

    private BigDecimal calculateUnitPrice(BigDecimal totalPrice, Integer quantity) {

        if (totalPrice == null) {
            return BigDecimal.ZERO;
        }

        if (quantity == null || quantity <= 0) {
            return BigDecimal.ZERO;
        }

        return totalPrice.divide(BigDecimal.valueOf(quantity), 2, java.math.RoundingMode.HALF_UP);
    }

    private void validateSaveOrUpdateCartRequest(CoCartUpdateRequestDto dto) {

        if (dto == null) {

            log.error("VALIDATION_FAILED | REQUEST_BODY_NULL");

            throw new CartException("Request body cannot be null");
        }

        validateCustomerId(dto.getCustomerId());

        if (dto.getOutletId() == null || dto.getOutletId() <= 0) {

            throw new CartException("Invalid outlet ID");
        }

        if (dto.getProductId() == null || dto.getProductId() <= 0) {

            throw new CartException("Invalid product ID");
        }

        if (dto.getVariants() == null || dto.getVariants().isEmpty()) {

            throw new CartException("At least one variant is required");
        }

        /*
         * Prevent duplicate variant entries
         * in the same request.
         */
        java.util.Set<Integer> variantIds = new java.util.HashSet<>();

        boolean normalProductVariantFound = false;

        for (CoCartVariantDto variant : dto.getVariants()) {

            if (variant == null) {

                throw new CartException("Variant item cannot be null");
            }

            Integer variantOptionId = variant.getVariantOptionId();

            /*
             * Normal product:
             * variantOptionId = NULL
             */
            if (variantOptionId == null) {

                if (normalProductVariantFound) {

                    throw new CartException("Duplicate normal product entry");
                }

                normalProductVariantFound = true;

            } else {

                if (variantOptionId <= 0) {

                    throw new CartException("Invalid variant option ID");
                }

                if (!variantIds.add(variantOptionId)) {

                    throw new CartException("Duplicate variant option ID: " + variantOptionId);
                }
            }

            if (variant.getQuantity() == null || variant.getQuantity() < 0) {

                throw new CartException(COConstants.MSG_INVALID_QUANTITY);
            }

            /*
             * Unit price is required only when
             * quantity > 0.
             *
             * For quantity = 0, it is used only
             * for remove operation and can be ignored.
             */
            if (variant.getQuantity() > 0 && (variant.getUnitPrice() == null || variant.getUnitPrice().compareTo(BigDecimal.ZERO) < 0)) {

                throw new CartException("Unit price cannot be negative");
            }
        }
    }

    private void validateCustomerId(Integer customerId) {

        if (customerId == null || customerId <= 0) {

            log.error("VALIDATION_FAILED | INVALID_CUSTOMER_ID | customerId={}", customerId);

            throw new CartException("Invalid customer ID");
        }
    }

    private void validateCartOutlet(CoCustomerCart existingCart, Integer requestedOutletId) {
        if (existingCart.getOutletId() == null) {
            log.error("CART_OUTLET_ID_NULL | cartId={} | requestedOutletId={}", existingCart.getCartId(), requestedOutletId);

            throw new CartException("Cart outlet information not found");
        }

        if (!existingCart.getOutletId().equals(requestedOutletId)) {
            log.error("CART_OUTLET_MISMATCH | cartId={} | cartOutletId={} | requestedOutletId={}", existingCart.getCartId(), existingCart.getOutletId(), requestedOutletId);

            throw new CartException("Selected outlet does not match the cart item outlet");
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