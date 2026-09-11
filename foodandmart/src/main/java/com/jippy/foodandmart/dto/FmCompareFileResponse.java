package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for comparing an uploaded Master Product
 * CSV/Excel file with the existing database records.
 *
 * The CompareItem contains:
 *
 * 1. Fields belonging to jippy_fm.master_products
 * 2. CSV/cache fields used for merchant pricing
 *    and availability information.
 *
 * Merchant price, timing and day-of-week are NOT stored
 * in the master_products table. They are retained here
 * for the cache/pricing/availability flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmCompareFileResponse {

    /**
     * Products that already exist in the database.
     */
    private List<CompareItem> duplicates;

    /**
     * Products that are present in the uploaded file
     * but do not exist in the database.
     */
    private List<CompareItem> newProducts;

    /**
     * Total number of products processed from the file.
     */
    private int totalInFile;

    /**
     * Number of duplicate products found.
     */
    private int duplicateCount;

    /**
     * Number of new products found.
     */
    private int newCount;

    /**
     * Number of rows skipped during file processing.
     */
    private int skippedCount;


    /**
     * Individual product information.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompareItem {

        /**
         * Existing database ID.
         *
         * For a new product this will be null.
         *
         * DB:
         * master_products.master_product_id
         */
        private Integer masterProductId;


        // ============================================================
        // MASTER PRODUCT TABLE FIELDS
        // ============================================================

        /**
         * DB:
         * master_product_name
         */
        private String masterProductName;

        /**
         * DB:
         * description
         */
        private String description;

        /**
         * DB:
         * photo
         */
        private String photo;

        /**
         * DB:
         * category_id
         */
        private Integer categoryId;

        /**
         * DB:
         * category_name
         */
        private String categoryName;

        /**
         * DB:
         * is_veg
         *
         * true  = Vegetarian
         * false = Non-Vegetarian
         */
        private Boolean isVeg;

        /**
         * DB:
         * cuisine_type
         */
        private String cuisineType;

        /**
         * DB:
         * has_options
         *
         * 0 = No options
         * 1 = Has options
         */
        private Integer hasOptions;

        /**
         * DB:
         * options
         *
         * Stored as JSONB in PostgreSQL.
         */
        private String options;

        /**
         * DB:
         * product_type
         */
        private String productType;


        // ============================================================
        // CSV / CACHE FIELDS
        // ============================================================

        /**
         * Merchant price from the uploaded CSV/Excel file.
         *
         * This is NOT stored in master_products.
         * It is retained for the pricing/cache flow.
         */
        private Double merchantPrice;

        /**
         * Product availability timing from the CSV/Excel file.
         *
         * Example:
         *
         * 09:00-22:00
         *
         * This is NOT stored in master_products.
         * It is retained for the cache/availability flow.
         */
        private String csvTiming;

        /**
         * Day of week from the CSV/Excel file.
         *
         * Example:
         *
         * Monday
         *
         * This is NOT stored in master_products.
         * It is retained for the cache/availability flow.
         */
        private String csvDayOfWeek;
    }
}