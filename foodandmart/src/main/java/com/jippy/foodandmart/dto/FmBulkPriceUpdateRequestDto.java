package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FmBulkPriceUpdateRequestDto {

    @NotEmpty
    private List<Integer> outletIds;

    @NotNull
    private String priceModel; // "FLAT" or "PERCENT"

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal value;
}