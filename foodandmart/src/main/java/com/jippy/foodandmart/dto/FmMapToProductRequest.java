package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmMapToProductRequest {

    /**
     * Existing mobile application may send this.
     *
     * Bulk upload can leave this null when products
     * belong to different categories.
     */
    private Integer outletCategoryId;

    /**
     * Outlet ID.
     */
    private Integer outletId;

    /**
     * Selected master/outlet products.
     */
    private List<ProductEntry> products;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductEntry {

        private String productName;

        private String description;

        private BigDecimal merchantPrice;

        private Boolean isVeg;

        /**
         * Kept for mobile compatibility.
         *
         * Master-product mapping will always create
         * the outlet product without variants.
         */
        private Boolean hasProductVariants;

        /**
         * Kept for compatibility.
         *
         * Not used during master-product mapping.
         */
        private List<VariantEntry> variants;

        /**
         * Master product ID.
         */
        private Integer masterProductId;

        /**
         * Master product category ID.
         */
        private Integer categoryId;

        /**
         * Product type from master_products.product_type.
         */
        private String productType;

        /**
         * CSV day.
         */
        private String csvDayOfWeek;

        /**
         * CSV timing.
         */
        private String csvTiming;

        /**
         * Explicit timing rows.
         */
        private List<TimingEntry> timings;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimingEntry {

        private Integer dayOfWeekId;

        private String startTime;

        private String endTime;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VariantEntry {

        private String variantName;

        private BigDecimal merchantPrice;
    }
}