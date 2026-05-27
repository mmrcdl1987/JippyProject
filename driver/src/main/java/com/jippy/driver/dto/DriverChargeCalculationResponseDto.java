package com.jippy.driver.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DriverChargeCalculationResponseDto {

    private BigDecimal pickupDistanceKm;

    private BigDecimal deliveryDistanceKm;

    private BigDecimal pickupUnitPrice;

    private BigDecimal deliveryUnitPrice;

    private BigDecimal pickupCharge;

    private BigDecimal deliveryCharge;

    private BigDecimal taxAmount;

    private BigDecimal totalDriverCharge;

    private Boolean codAvailable;
}