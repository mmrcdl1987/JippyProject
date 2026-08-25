package com.jippy.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NWalletPointsEvent {

    // Order that generated the points
    private String orderId;

    // Customer who earned the points
    private Integer customerId;

    // Points earned from this order
    private Integer transactionPoints;

    // Wallet transaction type
    private String pointsType;

    // Notification type
    private String notificationType;

    // FCM token captured during customer creation
    private String fcmToken;

    // Money credited when points are converted
    private BigDecimal convertedAmount;
}
