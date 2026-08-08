package com.jippy.foodandmart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FmPriceUpdateRequestDto {

    @NotEmpty
    private List<@NotNull Integer> outletIds;

    @NotEmpty
    private List<@Valid Item> items;

    @Data
    public static class Item {

        @NotNull
        private Integer productId;

        /**
         * NULL  = Base product price
         * NOT NULL = Variant/Add-on price
         */
        private Integer productVariantId;

        /**
         * Final online price.
         *
         * This API directly updates the supplied price.
         * Bulk API performs FLAT/PERCENT calculation.
         */
        @NotNull
        @DecimalMin(value = "0.01")
        private BigDecimal newPrice;
    }
}