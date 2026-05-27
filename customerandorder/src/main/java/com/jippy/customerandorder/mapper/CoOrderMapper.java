package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.CoOrderItemDto;
import com.jippy.customerandorder.dto.CoPlaceOrderRequestDto;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.entity.CoOrderItem;
import com.jippy.customerandorder.entity.CoOrderPriceBreakup;
import com.jippy.customerandorder.exception.OrderException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class CoOrderMapper {

    // ================= ORDER =================
    public CoOrder mapToOrder(CoPlaceOrderRequestDto placeOrderRequest) {

        CoOrder order = new CoOrder();

        order.setCustomerId(placeOrderRequest.getCustomerId());

        order.setOutletId(placeOrderRequest.getOutletId());

        order.setPaymentModeId(placeOrderRequest.getPaymentModeId());

        order.setOrderStatus(COConstants.ORDER_STATUS_PLACED);

        order.setCustomerDeliveryAddressId(placeOrderRequest.getCustomerDeliveryAddressId());

        order.setCustomerPhoneNumber(placeOrderRequest.getCustomerPhone());

        order.setCreatedAt(LocalDateTime.now());

        return order;
    }
    // ================= ORDER ITEMS =================

    public CoOrderItem mapToItem(CoOrderItemDto orderItemDto, String orderId) {

        validateOrderItem(orderItemDto);

        CoOrderItem orderItem = new CoOrderItem();

        orderItem.setOrderId(orderId);

        orderItem.setProductId(orderItemDto.getProductId());

        orderItem.setQuantity(orderItemDto.getQuantity());

        orderItem.setOnlineUnitPrice(orderItemDto.getOnlineUnitPrice());

        orderItem.setMerchantUnitPrice(orderItemDto.getMerchantUnitPrice());

        // ONLINE TOTAL
        orderItem.setOnlinePriceTotal(calculateTotalPrice(orderItemDto.getOnlineUnitPrice(), orderItemDto.getQuantity()));

        // MERCHANT TOTAL
        orderItem.setMerchantPriceTotal(calculateTotalPrice(orderItemDto.getMerchantUnitPrice(), orderItemDto.getQuantity()));

        orderItem.setCreatedAt(LocalDateTime.now());

        return orderItem;
    }

    // ================= PRICE BREAKUP =================

    public CoOrderPriceBreakup mapToPrice(CoPlaceOrderRequestDto placeOrderRequestDto, String orderId) {

        CoOrderPriceBreakup priceBreakup = new CoOrderPriceBreakup();

        priceBreakup.setOrderId(orderId);

        priceBreakup.setCouponId(placeOrderRequestDto.getCouponId());

        priceBreakup.setOrderAmount(placeOrderRequestDto.getOrderAmount());

        priceBreakup.setPlatformFee(defaultValue(placeOrderRequestDto.getPlatformFee()));

        priceBreakup.setDeliveryFee(defaultValue(placeOrderRequestDto.getDeliveryFee()));

        priceBreakup.setSurgeFee(defaultValue(placeOrderRequestDto.getSurgeFee()));

        priceBreakup.setPackagingFee(defaultValue(placeOrderRequestDto.getPackagingFee()));

        priceBreakup.setGst(defaultValue(placeOrderRequestDto.getGst()));

        priceBreakup.setOrderTotalAmount(placeOrderRequestDto.getOrderTotalAmount());

        priceBreakup.setCouponDiscount(defaultValue(placeOrderRequestDto.getCouponDiscount()));

        priceBreakup.setCreatedAt(LocalDateTime.now());

        return priceBreakup;
    }

    // ================= VALIDATIONS =================

    private void validateOrderItem(CoOrderItemDto orderItemDto) {

        if (orderItemDto == null) {

            throw new OrderException("Order item cannot be null");
        }

        if (orderItemDto.getOnlineUnitPrice() == null) {

            throw new OrderException("Online unit price cannot be null");
        }

        if (orderItemDto.getMerchantUnitPrice() == null) {

            throw new OrderException("Merchant unit price cannot be null");
        }

        if (orderItemDto.getQuantity() == null || orderItemDto.getQuantity() <= 0) {

            throw new OrderException("Quantity must be greater than zero");
        }
    }

    // ================= CALCULATIONS =================

    private BigDecimal calculateTotalPrice(BigDecimal unitPrice, Integer quantity) {

        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // ================= COMMON METHODS =================

    private BigDecimal defaultValue(BigDecimal value) {

        return value == null ? BigDecimal.ZERO : value;
    }
}