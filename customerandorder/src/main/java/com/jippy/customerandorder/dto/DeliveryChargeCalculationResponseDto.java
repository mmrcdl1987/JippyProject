package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeliveryChargeCalculationResponseDto {

    private BigDecimal deliveryDistanceKm;

    private BigDecimal deliveryCharge;

    private BigDecimal taxAmount;

    private BigDecimal totalDeliveryCharge;

    private Boolean codAvailable;
}