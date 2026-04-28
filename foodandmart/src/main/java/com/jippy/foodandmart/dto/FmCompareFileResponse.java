package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Compare response — CompareItem now carries all product fields so that
 * /add-new-items can persist the full catalogue data from the CSV.
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
        private String  masterProductName;
        private Integer veg;
        private Integer nonVeg;
        private Integer categoryId;
        private String  categoryName;
        private Integer subCategoryId;
        private String  subCategoryName;
        private String  description;
        private String  shortDescription;
        private String  photo;
        private String  photos;
        private String  thumbnail;
        private String  foodType;
        private String  cuisineType;
        private Integer hasOptions;
        private Integer optionsEnabled;
        private String  options;
        private Integer calories;
        private Integer protein;
        private Integer fats;
        private Integer carbs;
        private Integer grams;
        private Integer publish;
        private Double  merchantPrice;   // price from the uploaded CSV file
        private String  csvTiming;        // availability timing from the uploaded CSV file (e.g. "9:00-22:00")
        private String  csvDayOfWeek;     // day-of-week name from the daysofaweek CSV column (e.g. "Monday")
    }
}
