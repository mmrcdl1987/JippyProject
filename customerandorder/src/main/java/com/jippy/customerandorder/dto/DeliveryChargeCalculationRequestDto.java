package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeliveryChargeCalculationRequestDto {

    @NotNull(message = "Outlet id is required")
    private Integer outletId;

    @NotNull(message = "Customer address id is required")
    private Integer customerAddressId;

    @NotNull(message = "Order amount is required")
    private BigDecimal orderAmount;
}