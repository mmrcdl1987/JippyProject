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
     * Outlet category ID.
     *
     * When provided, all selected products are mapped
     * into this outlet category.
     *
     * Can be null when products belong to different categories
     * and the backend needs to resolve the outlet category
     * from each master product.
     */
    private Integer outletCategoryId;

    /**
     * Outlet ID.
     */
    private Integer outletId;

    /**
     * Selected master products.
     */
    private List<ProductEntry> products;


    // ============================================================
    // PRODUCT ENTRY
    // ============================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductEntry {

        /**
         * Master product ID.
         *
         * Primary reference used by backend to fetch
         * the actual master product.
         */
        private Integer masterProductId;

        /**
         * Product name.
         *
         * Backend should prefer:
         * master_products.master_product_name
         */
        private String productName;

        /**
         * Product description.
         *
         * Backend should prefer:
         * master_products.description
         */
        private String description;

        /**
         * Master product category ID.
         */
        private Integer categoryId;

        /**
         * Master product category name.
         *
         * Example:
         * Rice & Noodles
         */
        private String categoryName;

        /**
         * Product type from master_products.product_type.
         *
         * Examples:
         * RICE
         * CURRY
         * BREAKFAST
         * NOODLES
         * DESSERT
         * BEVERAGE
         */
        private String productType;

        /**
         * Veg / Non-Veg.
         *
         * Backend should prefer the value from
         * master_products.is_veg.
         */
        private Boolean isVeg;

        /**
         * Merchant price supplied from CSV/UI.
         *
         * This value should be saved into:
         * jippy_fm.products.merchant_price
         */
        private BigDecimal merchantPrice;

        /**
         * Kept for mobile compatibility.
         *
         * Master-product mapping normally creates
         * the outlet product without variants.
         */
        private Boolean hasProductVariants;

        /**
         * Kept for compatibility.
         */
        private List<VariantEntry> variants;

        /**
         * Product image supplied by client.
         *
         * During master-product mapping the backend
         * should prefer master_products.photo.
         */
        private String imageLink;

        /**
         * CSV timing.
         *
         * Example:
         * 11:00-22:00
         */
        private String csvTiming;

        /**
         * CSV day of week.
         *
         * Example:
         * Monday
         */
        private String csvDayOfWeek;

        /**
         * Explicit timing rows.
         *
         * Used when the request already contains
         * resolved day IDs and start/end times.
         */
        private List<TimingEntry> timings;

        /**
         * Variant groups.
         *
         * Kept for compatibility with existing clients.
         */
        private List<?> variantGroups;
    }


    // ============================================================
    // TIMING ENTRY
    // ============================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimingEntry {

        /**
         * Day ID from days_of_week table.
         *
         * 1 = Monday
         * ...
         * 7 = Sunday
         *
         * 0 can be used for all days.
         */
        private Integer dayOfWeekId;

        /**
         * Start time.
         *
         * Example:
         * 11:00
         */
        private String startTime;

        /**
         * End time.
         *
         * Example:
         * 22:00
         */
        private String endTime;
    }


    // ============================================================
    // VARIANT ENTRY
    // ============================================================

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