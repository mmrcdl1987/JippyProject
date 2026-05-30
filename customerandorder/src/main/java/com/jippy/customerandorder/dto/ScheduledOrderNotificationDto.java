package com.jippy.customerandorder.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduledOrderNotificationDto {

    private String orderId;

    private Integer customerId;

    private Integer outletId;

    private Integer mealSubscriptionId;

    private String notificationType;

    private LocalDateTime scheduledDeliveryDateTime;
}