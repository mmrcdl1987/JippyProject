package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.exception.OrderException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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

    public static CoNewOrderEvent mapToOrderNewOrderEvent(CoOrder order, CoCustomer customer, List<CoOrderItemsEvent> orderItemsEventList) {

        CoNewOrderEvent newOrderEvent = new CoNewOrderEvent();

        newOrderEvent.setOrderId(order.getOrderId());
        newOrderEvent.setCustomerId(order.getCustomerId());
        newOrderEvent.setCustomerName(customer.getFirstName());
        newOrderEvent.setCustomerMobileNum(customer.getPhoneNumber());
        newOrderEvent.setOrderStatus(order.getOrderStatus());
        newOrderEvent.setOrderItemsList(orderItemsEventList);
        newOrderEvent.setCookingInstructions(order.getCookingInstructions());
        newOrderEvent.setCutleryRequired(order.getIsCutleryRequired());

        return newOrderEvent;
    }
}