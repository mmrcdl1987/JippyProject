
        package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FmMasterProductRequest {

    private String masterProductName;

    private String description;

    private String shortDescription;

    private String photo;

    private String photos;

    private String thumbnail;

    private Integer categoryId;

    private String categoryName;

    private Integer subCategoryId;

    private String subCategoryName;

    private Integer veg = 0;

    private Integer nonVeg = 0;

    private String foodType;

    private String cuisineType;

    private Integer hasOptions = 0;

    private Integer optionsEnabled = 0;

    private String options;

    private Integer calories = 0;

    private Integer protein = 0;

    private Integer fats = 0;

    private Integer carbs = 0;

    private Integer grams = 0;

    /**
     * Product type.
     *
     * This value is stored in:
     * jippy_fm.master_products.product_type
     *
     * Example:
     * FOOD
     * BEVERAGE
     * GROCERY
     * DESSERT
     */
    private String productType;

    private Integer publish = 1;

    private Integer createdBy;

    private Integer updatedBy;
}

