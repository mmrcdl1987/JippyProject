package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FmBulkPriceUpdateRequestDto {

    @NotEmpty
    private List<@NotNull Integer> outletIds;

    @NotNull
    @Pattern(
            regexp = "FLAT|PERCENTAGE",
            message = "priceModel must be FLAT or PERCENT"
    )
    private String priceModel;

    @NotNull
    @DecimalMin(
            value = "0.01",
            message = "value must be greater than 0"
    )
    private BigDecimal value;
}