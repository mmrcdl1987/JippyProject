package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmProductVariantOptionRequestDto {

    /**
     * Null = Create
     * Not Null = Update
     */
    private Integer productVariantOptionsId;

    @NotNull(message = "Product Variant Group Value Id is required")
    private Integer productVariantGroupValuesId;

    @NotBlank(message = "Price Type is required")
    private String priceType;

    @NotNull(message = "Variant Price is required")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal variantPrice;
}