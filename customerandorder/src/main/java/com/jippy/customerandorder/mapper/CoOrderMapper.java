package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.CoOrderItemDto;
import com.jippy.customerandorder.dto.CoPlaceOrderRequestDto;
import com.jippy.customerandorder.entity.CoMealSubscription;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.entity.CoOrderItem;
import com.jippy.customerandorder.entity.CoOrderPriceBreakup;
import com.jippy.customerandorder.exception.OrderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Slf4j
public class CoOrderMapper {

    /*
     * MAP ORDER
     */
    public CoOrder mapToOrder(CoPlaceOrderRequestDto requestDto) {

        log.info("MAPPER_START | MAP_ORDER | customerId={} | outletId={}", requestDto.getCustomerId(), requestDto.getOutletId());

        CoOrder order = new CoOrder();

        order.setCustomerId(requestDto.getCustomerId());

        order.setOutletId(requestDto.getOutletId());

        order.setPaymentModeId(requestDto.getPaymentModeId());

        order.setOrderStatus(COConstants.ORDER_STATUS_PLACED);

        order.setCustomerDeliveryAddressId(requestDto.getCustomerDeliveryAddressId());

        order.setCustomerPhoneNumber(requestDto.getCustomerPhone());

        order.setOrderType(requestDto.getOrderType());

        order.setScheduledDeliveryDateTime(requestDto.getScheduledDeliveryDateTime());

        order.setDistanceKms(requestDto.getDeliveryDistanceKms()!= null ?requestDto.getDeliveryDistanceKms().doubleValue():0);

        order.setCreatedAt(LocalDateTime.now());

        order.setCreatedBy(requestDto.getCustomerId());
        //exists only in group ordering
        order.setGroupOrderInvitationId(requestDto.getGroupOrderInvitationId());

        log.info("MAPPER_END | MAP_ORDER_SUCCESS | customerId={}", requestDto.getCustomerId());

        return order;
    }

    /*
     * MAP ORDER ITEM
     */
    public CoOrderItem mapToItem(CoOrderItemDto orderItemDto, String orderId) {

        validateOrderItem(orderItemDto);

        log.info("MAPPER_START | MAP_ORDER_ITEM | orderId={} | productId={}", orderId, orderItemDto.getProductId());

        CoOrderItem orderItem = new CoOrderItem();

        orderItem.setOrderId(orderId);

        orderItem.setProductId(orderItemDto.getProductId());

        orderItem.setQuantity(orderItemDto.getQuantity());

        orderItem.setOnlineUnitPrice(defaultValue(orderItemDto.getOnlineUnitPrice()));

        orderItem.setMerchantUnitPrice(defaultValue(orderItemDto.getMerchantUnitPrice()));

        /*
         * ONLINE TOTAL
         */
        orderItem.setOnlinePriceTotal(calculateTotalPrice(orderItemDto.getOnlineUnitPrice(), orderItemDto.getQuantity()));

        /*
         * MERCHANT TOTAL
         */
        orderItem.setMerchantPriceTotal(calculateTotalPrice(orderItemDto.getMerchantUnitPrice(), orderItemDto.getQuantity()));

        orderItem.setCreatedAt(LocalDateTime.now());

        log.info("MAPPER_END | MAP_ORDER_ITEM_SUCCESS | orderId={} | productId={}", orderId, orderItemDto.getProductId());

        return orderItem;
    }

    /*
     * MAP PRICE BREAKUP
     */
    public CoOrderPriceBreakup mapToPrice(CoPlaceOrderRequestDto requestDto, String orderId) {

        log.info("MAPPER_START | MAP_PRICE_BREAKUP | orderId={}", orderId);

        CoOrderPriceBreakup breakup = new CoOrderPriceBreakup();

        breakup.setOrderId(orderId);

        breakup.setCouponId(requestDto.getCouponId());

        breakup.setOrderAmount(defaultValue(requestDto.getOrderAmount()));

        breakup.setPlatformFee(defaultValue(requestDto.getPlatformFee()));

        breakup.setDeliveryFee(defaultValue(requestDto.getDeliveryFee()));

        breakup.setSurgeFee(defaultValue(requestDto.getSurgeFee()));

        breakup.setPackagingFee(defaultValue(requestDto.getPackagingFee()));

        breakup.setGst(defaultValue(requestDto.getGst()));

        breakup.setOrderTotalAmount(defaultValue(requestDto.getOrderTotalAmount()));

        breakup.setCouponDiscount(defaultValue(requestDto.getCouponDiscount()));

        breakup.setCreatedAt(LocalDateTime.now());

        breakup.setPickUpDistanceKms(requestDto.getPickUpDistanceKms());

        breakup.setDeliveryDistanceKms(requestDto.getDeliveryDistanceKms());

        breakup.setPickUpCharges(requestDto.getPickUpCharges());

        breakup.setDeliveryCharges(requestDto.getDeliveryCharges());

        log.info("MAPPER_END | MAP_PRICE_BREAKUP_SUCCESS | orderId={}", orderId);

        return breakup;
    }

    /*
     * MAP SUBSCRIPTION
     */
    public CoMealSubscription mapToSubscription(CoPlaceOrderRequestDto requestDto) {

        log.info("MAPPER_START | MAP_SUBSCRIPTION | customerId={}", requestDto.getCustomerId());

        CoMealSubscription subscription = new CoMealSubscription();

        subscription.setCustomerId(requestDto.getCustomerId());

        subscription.setOutletId(requestDto.getOutletId());

        subscription.setMealPreference(requestDto.getMealPreference());

        subscription.setSubscriptionStartDate(requestDto.getSubscriptionStartDate());

        subscription.setSubscriptionEndDate(requestDto.getSubscriptionEndDate());

        subscription.setSubscriptionStatus(COConstants.SUBSCRIPTION_STATUS_ACTIVE);

        subscription.setCreatedAt(LocalDateTime.now());

        subscription.setCreatedBy(requestDto.getCustomerId());

        log.info("MAPPER_END | MAP_SUBSCRIPTION_SUCCESS | customerId={}", requestDto.getCustomerId());

        return subscription;
    }

    /*
     * VALIDATE ORDER ITEM
     */
    private void validateOrderItem(CoOrderItemDto orderItemDto) {

        if (orderItemDto == null) {

            log.error("VALIDATION_FAILED | ORDER_ITEM_NULL");

            throw new OrderException("Order item cannot be null");
        }

        if (orderItemDto.getQuantity() == null || orderItemDto.getQuantity() <= 0) {

            log.error("VALIDATION_FAILED | INVALID_QUANTITY | productId={}", orderItemDto.getProductId());

            throw new OrderException("Quantity must be greater than zero");
        }
    }

    /*
     * CALCULATE TOTAL
     */
    private BigDecimal calculateTotalPrice(BigDecimal unitPrice, Integer quantity) {

        return defaultValue(unitPrice).multiply(BigDecimal.valueOf(quantity));
    }

    /*
     * DEFAULT VALUE
     */
    private BigDecimal defaultValue(BigDecimal value) {

        return value == null ? BigDecimal.ZERO : value;
    }
}