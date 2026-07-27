package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.COOrderEvent;
import com.jippy.customerandorder.dto.WelcomeCouponDto;
import com.jippy.customerandorder.dto.WelcomeCouponNotificationEvent;
import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.exception.OrderException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class COEventMapper {

    private COEventMapper() {
    }

    public static COOrderEvent mapToOrderEvent(CoOrder order) {

        if (order == null) {

            log.error("VALIDATION_FAILED | ORDER_NULL");

            throw new OrderException("Order cannot be null");
        }

        log.info("MAPPER_START | MAP_ORDER_EVENT | orderId={}", order.getOrderId());

        COOrderEvent event = new COOrderEvent();

        event.setOrderId(order.getOrderId());

        event.setCustomerId(order.getCustomerId());

        event.setOutletId(order.getOutletId());

        event.setDriverId(order.getDriverId());

        event.setStatus(order.getOrderStatus());

        /*
         * ORDER TYPE
         */
        event.setOrderType(order.getOrderType());

        /*
         * SCHEDULED DELIVERY
         */
        event.setScheduledDeliveryDateTime(order.getScheduledDeliveryDateTime());

        /*
         * SUBSCRIPTION
         */
        event.setMealSubscriptionId(order.getMealSubscriptionId());

        log.info("MAPPER_END | MAP_ORDER_EVENT_SUCCESS | orderId={}", order.getOrderId());

        return event;
    }

    public static WelcomeCouponNotificationEvent mapToWelcomeCouponEvent(
            CoCustomer customer,
            WelcomeCouponDto coupon) {

        WelcomeCouponNotificationEvent event =
                new WelcomeCouponNotificationEvent();

        event.setCustomerId(customer.getCustomerId());

        event.setCustomerName(customer.getFirstName());

        event.setCouponId(coupon.getCouponId());

        event.setCouponCode(coupon.getCouponCode());

        event.setDiscountValue(coupon.getDiscountValue());

        event.setMinOrderValue(coupon.getMinOrderValue());

        return event;
    }
}