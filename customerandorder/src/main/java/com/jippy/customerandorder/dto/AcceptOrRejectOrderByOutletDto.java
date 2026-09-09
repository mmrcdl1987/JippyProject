package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AcceptOrRejectOrderByOutletDto {

    @NotNull(message = "Order Id is required")
    private String orderId;

    @NotNull(message = "Outlet Id is required")
    private Integer outletId;

    @NotNull(message = "Order status is required")
    private String orderStatus; // "ACCEPT" or "REJECT"

    private String rejectionReason; // Optional, only required if action is "REJECT"

    @NotNull(message = "Preparation time is required")
    @Min(value = 1, message = "Preparation time must be at least 1 minute")
    @Max(value = 15, message = "Preparation time cannot exceed 15 minutes")
    private Integer preparationTimeInMins;
}
