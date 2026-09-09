package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerDeliveryChargeCalculationResponseDto {

    private BigDecimal deliveryDistanceKm;

    private BigDecimal grossDeliveryCharge;

    private BigDecimal freeDistanceKms;

    private BigDecimal freeDistanceBenefit;

    private BigDecimal chargePerKm;

    private BigDecimal deliveryCharge;
}