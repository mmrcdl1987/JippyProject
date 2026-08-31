package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Compare response.
 *
 * CompareItem carries all product fields so that
 * /add-new-items can persist the full catalogue data
 * from the CSV.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmCompareFileResponse {

    private List<CompareItem> duplicates;
    private List<CompareItem> newProducts;

    private int totalInFile;
    private int duplicateCount;
    private int newCount;
    private int skippedCount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompareItem {

        private Integer masterProductId;       // null for new items

        private String masterProductName;

        private Integer veg;

        private Integer nonVeg;

        private Integer categoryId;

        private String categoryName;

        private Integer subCategoryId;

        private String subCategoryName;

        private String description;

        private String shortDescription;

        private String photo;

        private String photos;

        private String thumbnail;

        private String foodType;

        private String cuisineType;

        private Integer hasOptions;

        private Integer optionsEnabled;

        private String options;

        private Integer calories;

        private Integer protein;

        private Integer fats;

        private Integer carbs;

        private Integer grams;

        /**
         * Product type from CSV.
         *
         * Example:
         * FOOD
         * BEVERAGE
         * GROCERY
         */
        private String productType;

        private Integer publish;

        /**
         * Price from the uploaded CSV file.
         */
        private Double merchantPrice;

        /**
         * Availability timing from the uploaded CSV file.
         *
         * Example:
         * 9:00-22:00
         */
        private String csvTiming;

        /**
         * Day-of-week name from the CSV file.
         *
         * Example:
         * Monday
         */
        private String csvDayOfWeek;
    }
}