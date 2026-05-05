package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FmPriceUpdateRequestDto {

    @NotEmpty
    private List<Integer> outletIds;

    @NotEmpty
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull
        private Integer productId;

        @NotNull
        @DecimalMin("0.01")
        private BigDecimal newPrice;
    }
}