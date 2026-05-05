package com.jippy.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NOrderEvent {

    private String orderId;
    private Integer customerId;
    private Integer outletId;
    private Integer driverId;
    private String status;        // e.g., "PLACED", "PICKED_UP", "DELIVERED"
   // private String cityName;      // Useful for routing to regional FCM topics
}
