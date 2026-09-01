package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoOrderCheckoutFeeRequestDto {

    @NotNull(message = "Platform fee is required")
    @DecimalMin(value = "0.00", message = "Platform fee cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Platform fee must have maximum 8 integer digits and 2 decimal places")
    private BigDecimal platformFee;

    @NotNull(message = "Platform fee toggle is required")
    private Boolean platformFeeToggle;

    @NotNull(message = "Surge fee is required")
    @DecimalMin(value = "0.00", message = "Surge fee cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Surge fee must have maximum 8 integer digits and 2 decimal places")
    private BigDecimal surgeFee;

    @NotNull(message = "Surge fee toggle is required")
    private Boolean surgeFeeToggle;

    @NotNull(message = "Packaging fee is required")
    @DecimalMin(value = "0.00", message = "Packaging fee cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Packaging fee must have maximum 8 integer digits and 2 decimal places")
    private BigDecimal packagingFee;

    @NotNull(message = "Packaging fee toggle is required")
    private Boolean packagingFeeToggle;

    @NotNull(message = "Area id is required")
    private Integer areaId;

    private Integer userId;
}