package com.jippy.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverDeliveryChargeSettingsListResponseDto {

    private Integer deliveryChargeSettingId;

    private Integer areaId;

    private Integer stateId;

    private String stateName;

    private Integer cityId;

    private String cityName;

    private String areaName;

    private BigDecimal pickUpKmsRangeFrom;

    private BigDecimal pickUpKmsRangeTo;

    private BigDecimal unitPricePerPickKm;

    private BigDecimal deliveryKmsRangeFrom;

    private BigDecimal deliveryKmsRangeTo;

    private BigDecimal unitPricePerDeliverKm;

    private LocalDateTime createdAt;

    private Integer createdBy;

    private LocalDateTime updatedAt;

    private Integer updatedBy;
}