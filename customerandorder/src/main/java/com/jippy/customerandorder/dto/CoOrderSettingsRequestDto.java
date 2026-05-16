package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;

@Data
public class CoOrderSettingsRequestDto {

    private Integer orderSettingsId;

    @DecimalMin(value = "0.0", inclusive = true, message = "Platform fee must be greater than or equal to 0")
    private BigDecimal platformFee;

    @DecimalMin(value = "0.0", inclusive = true, message = "Surge fee must be greater than or equal to 0")
    private BigDecimal surgeFee;

    @DecimalMin(value = "0.0", inclusive = true, message = "Packaging fee must be greater than or equal to 0")
    private BigDecimal packagingFee;
    @DecimalMin(value = "0.0", inclusive = true,
            message = "Delivery fee tax must be greater than or equal to 0")
    @DecimalMax(value = "100.0", inclusive = true,
            message = "Delivery fee tax cannot be greater than 100")
    private BigDecimal deliveryFeeTax;

    @DecimalMin(value = "0.0", inclusive = true,
            message = "Food total amount tax must be greater than or equal to 0")
    @DecimalMax(value = "100.0", inclusive = true,
            message = "Food total amount tax cannot be greater than 100")
    private BigDecimal foodTotalAmountTax;
    
    private Integer createdBy;

    private Integer updatedBy;
}