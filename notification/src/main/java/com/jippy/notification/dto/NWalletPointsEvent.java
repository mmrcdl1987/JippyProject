package com.jippy.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}