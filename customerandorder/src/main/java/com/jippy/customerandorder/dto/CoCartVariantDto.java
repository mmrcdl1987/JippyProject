package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoCartVariantDto {

    /**
     * NULL = normal product without variant.
     *
     * Positive value = selected product variant option.
     */
    @Min(value = 1, message = "Variant option id must be greater than zero")
    private Integer variantOptionId;

    /**
     * Quantity = 0 means remove this exact variant.
     */
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Unit price cannot be negative"
    )
    private BigDecimal unitPrice;
}