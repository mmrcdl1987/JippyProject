package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmProductVariantValueRequestDto {

    /**
     * Null -> Create
     * Not Null -> Update
     */
    private Integer productVariantGroupValuesId;

    @NotBlank(message = "Variant name is required")
    private String variantName;

}