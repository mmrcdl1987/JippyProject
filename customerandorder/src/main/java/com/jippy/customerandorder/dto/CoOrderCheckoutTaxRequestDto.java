package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.DecimalMax;
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
public class CoOrderCheckoutTaxRequestDto {

    @NotNull(message = "Platform fee tax percentage is required")
    @DecimalMin(value = "0.00", message = "Platform fee tax percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Platform fee tax percentage cannot exceed 100")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal platformFeeTax;

    @NotNull(message = "Surge fee tax percentage is required")
    @DecimalMin(value = "0.00", message = "Surge fee tax percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Surge fee tax percentage cannot exceed 100")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal surgeFeeTax;

    @NotNull(message = "Packaging fee tax percentage is required")
    @DecimalMin(value = "0.00", message = "Packaging fee tax percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Packaging fee tax percentage cannot exceed 100")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal packagingFeeTax;

    @NotNull(message = "Delivery fee tax percentage is required")
    @DecimalMin(value = "0.00", message = "Delivery fee tax percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Delivery fee tax percentage cannot exceed 100")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal deliveryFeeTax;

    @NotNull(message = "Food amount tax percentage is required")
    @DecimalMin(value = "0.00", message = "Food amount tax percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Food amount tax percentage cannot exceed 100")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal foodAmountTax;

    private Integer userId;
}