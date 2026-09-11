package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FmMasterProductResponseDto {

    /**
     * Database generated ID
     */
    private Integer masterProductId;

    /**
     * Master product name
     */
    private String masterProductName;

    /**
     * Category ID
     */
    private Integer categoryId;

    /**
     * Category name
     */
    private String categoryName;

    /**
     * Product image URL
     */
    private String photo;

    /**
     * Veg / Non-Veg
     * <p>
     * true  = Veg
     * false = Non-Veg
     */
    private Boolean isVeg;

    /**
     * Cuisine type
     */
    private String cuisineType;

    /**
     * Whether product has options
     * <p>
     * 0 = No
     * 1 = Yes
     */
    private Integer hasOptions;

    /**
     * Product options stored as JSON
     */
    private String options;

    /**
     * Product type
     */
    private String productType;
}