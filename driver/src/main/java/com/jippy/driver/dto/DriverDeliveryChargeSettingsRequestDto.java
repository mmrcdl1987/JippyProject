package com.jippy.driver.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DriverDeliveryChargeSettingsRequestDto {

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal pickUpKmsRangeFrom;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal pickUpKmsRangeTo;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal unitPricePerPickKm;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal deliveryKmsRangeFrom;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal deliveryKmsRangeTo;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal unitPricePerDeliverKm;

    private Integer createdBy;
}