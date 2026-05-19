package com.jippy.driver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DriverChargeCalculationRequestDto {

    @NotNull
    private Double driverLatitude;

    @NotNull
    private Double driverLongitude;

    @NotNull
    private Integer outletId;

    @NotNull
    private Integer customerAddressId;

    @NotNull
    private BigDecimal orderAmount;
}