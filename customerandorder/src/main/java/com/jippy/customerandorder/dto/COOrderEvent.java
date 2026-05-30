package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class COOrderEvent {

    /*
     * ORDER DETAILS
     */
    private String orderId;

    private Integer customerId;

    private Integer outletId;

    private Integer driverId;

    /*
     * PLACED
     * ACCEPTED
     * PICKED_UP
     * DELIVERED
     * CANCELLED
     */
    private String status;

    /*
     * AREA ROUTING
     */
    private Integer areaId;

    /*
     * SPECIALIZED OUTLET FLOW
     */
    private Integer rejectedOutletId;

    /*
     * NORMAL
     * SCHEDULED_RECURRING
     * SCHEDULED_CUSTOM_PLAN
     */
    private String orderType;

    /*
     * DELIVERY DATE TIME
     */
    private LocalDateTime scheduledDeliveryDateTime;

    /*
     * SUBSCRIPTION ID
     */
    private Integer mealSubscriptionId;

    /*
     * SCHEDULED FLAGS
     */
    private Boolean scheduledOrder;

    private Boolean morningReminder;

    private Boolean oneHourReminder;

    /*
     * ORDER_CREATED
     * SCHEDULED_ORDER_CREATED
     * SCHEDULED_ORDER_TODAY
     * SCHEDULED_ORDER_1_HOUR_BEFORE
     */
    private String notificationType;
}