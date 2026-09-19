package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAcceptedEvent {

    private String orderId;

    private Integer outletId;

    private LocalDateTime merchantAcceptedTime;

    private Integer preparationTimeInMins;

    private LocalDateTime deliveryRequestAt;
}
