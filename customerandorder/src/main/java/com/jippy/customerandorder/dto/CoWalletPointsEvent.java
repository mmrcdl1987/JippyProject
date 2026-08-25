package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoWalletPointsEvent {

    // Order that generated the wallet points
    private String orderId;

    // Customer who received the points
    private Integer customerId;

    // Points earned from this order
    private Integer transactionPoints;

    // Wallet transaction type
    private String pointsType;

    // Notification type to be processed by Notification MS
    private String notificationType;

    // FCM token captured during customer creation
    private String fcmToken;

    // Money credited when points are converted
    private BigDecimal convertedAmount;
}
