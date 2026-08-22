package com.jippy.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DriverDeliveryChargeSettingsResponseDto {

    private Integer deliveryChargeSettingId;

    private Long areaId;

    private BigDecimal pickUpKmsRangeFrom;

    private BigDecimal pickUpKmsRangeTo;

    private BigDecimal unitPricePerPickKm;

    private BigDecimal deliveryKmsRangeFrom;

    private BigDecimal deliveryKmsRangeTo;

    private BigDecimal unitPricePerDeliverKm;

    private LocalDateTime createdAt;

    private Integer createdBy;
}