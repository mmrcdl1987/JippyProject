package com.jippy.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NOrderEvent {

    private String orderId;

    private Integer customerId;

    private Integer outletId;

    private Integer driverId;

    private String status;

    private Integer areaId;

    private Integer rejectedOutletId;

    /*
     * NEW FIELDS
     */
    private String orderType;

    private LocalDateTime scheduledDeliveryDateTime;

    private Integer mealSubscriptionId;

    private Boolean scheduledOrder;

    private Boolean morningReminder;

    private Boolean oneHourReminder;

    private String notificationType;
}