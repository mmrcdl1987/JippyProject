package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FmBulkPriceUpdateRequestDto {

    @NotEmpty(message = "OutletIds cannot be empty")
    private List<Integer> outletIds;

    /**
     * FLAT
     * PERCENTAGE
     */
    @NotBlank(message = "Price model cannot be empty")
    private String priceModel;

    /**
     * Always positive.
     *
     * Example:
     * 10
     * 20
     */
    @NotNull(message = "Price value cannot be null")
    @DecimalMin(
            value = "0.01",
            message = "Price value must be greater than zero"
    )
    private BigDecimal value;

    /**
     * FLAT
     * PERCENTAGE
     */
    private String priceType;

    /**
     * Example:
     * OUTLET
     */
    private String locationType;

    /**
     * INCREASE
     * DECREASE
     */
    private String operationType;
}