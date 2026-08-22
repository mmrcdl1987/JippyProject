package com.jippy.driver.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class DriverDeliveryChargeSettingsGetByIdResponseDto {

    private Integer deliveryChargeSettingId;

    private BigDecimal kmsRangeFrom;

    private BigDecimal kmsRangeTo;

    private BigDecimal unitPricePerKm;

    private String chargeType;

    private String deliveryType;

    private String driverType;

    private String serviceType;

    private String vehicleType;

    private String fuelType;

    private Integer zoneId;

    private String currencyCode;

    private Integer waitingFreeMinutes;

    private BigDecimal waitingPerMinute;

    private BigDecimal nightCharge;

    private BigDecimal peakCharge;

    private BigDecimal weatherSurcharge;

    private BigDecimal remoteAreaCharge;

    private BigDecimal remoteZoneSurcharge;

    private String status;

    private LocalDateTime createdAt;

    private Integer createdBy;

    private LocalDateTime updatedAt;

    private Integer updatedBy;
}