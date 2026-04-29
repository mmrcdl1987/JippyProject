package com.jippy.customerandorder.mapper;
import com.jippy.customerandorder.Constants.COConstants;
import com.jippy.customerandorder.dto.CoOrderItemDto;
import com.jippy.customerandorder.dto.CoPlaceOrderRequestDto;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.entity.CoOrderItem;
import com.jippy.customerandorder.entity.CoOrderPriceBreakup;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class CoOrderMapper {

    // - ORDER
    public CoOrder mapToOrder(CoPlaceOrderRequestDto placeOrderRequest) {
        CoOrder order = new CoOrder();
        order.setCustomerId(placeOrderRequest.getCustomerId());
        order.setOutletId(placeOrderRequest.getOutletId());
        order.setOrderStatus(COConstants.ORDER_STATUS_PLACED);
        order.setCustomerDeliveryAddressId(placeOrderRequest.getCustomerDeliveryAddressId());
        order.setCustomerPhoneNumber(placeOrderRequest.getCustomerPhone());
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    // ORDER ITEMS
    public CoOrderItem mapToItem(CoOrderItemDto orderItemDto, String orderId) {

        CoOrderItem orderItem = new CoOrderItem();

        orderItem.setOrderId(orderId);
        orderItem.setProductId(orderItemDto.getProductId());
        orderItem.setQuantity(orderItemDto.getQuantity());
        orderItem.setOnlineUnitPrice(orderItemDto.getOnlineUnitPrice());
        orderItem.setMerchantUnitPrice(orderItemDto.getMerchantUnitPrice());
        //  IMPORTANT FIXES

        // Online total = qty * online price
        orderItem.setOnlinePriceTotal(
                orderItemDto.getOnlineUnitPrice()
                        .multiply(BigDecimal.valueOf(orderItemDto.getQuantity()))
        );
        // Merchant total = qty * merchant price
        orderItem.setMerchantPriceTotal(
                orderItemDto.getMerchantUnitPrice()
                        .multiply(BigDecimal.valueOf(orderItemDto.getQuantity()))
        );
        orderItem.setCreatedAt(LocalDateTime.now());
        return orderItem;
    }

    // - PRICE BREAKUP
    public CoOrderPriceBreakup mapToPrice(CoPlaceOrderRequestDto placeOrderRequestDto, String orderId) {

        CoOrderPriceBreakup priceBreakup = new CoOrderPriceBreakup();

        priceBreakup.setOrderId(orderId);
        priceBreakup.setCouponId(placeOrderRequestDto.getCouponId());
        priceBreakup.setOrderAmount(placeOrderRequestDto.getOrderAmount());
        priceBreakup.setPlatformFee(placeOrderRequestDto.getPlatformFee());
        priceBreakup.setDeliveryFee(placeOrderRequestDto.getDeliveryFee());
        priceBreakup.setSurgeFee(placeOrderRequestDto.getSurgeFee());
        priceBreakup.setPackagingFee(placeOrderRequestDto.getPackagingFee());
        priceBreakup.setGst(placeOrderRequestDto.getGst());
        priceBreakup.setOrderTotalAmount(placeOrderRequestDto.getOrderTotalAmount());
        priceBreakup.setCouponDiscount(placeOrderRequestDto.getCouponDiscount());
        priceBreakup.setCreatedAt(LocalDateTime.now());

        return priceBreakup;
    }
}