package com.jippy.foodandmart.dto;

import lombok.Data;

@Data
public class FmCreateMasterProductResponseDto {

    private Integer masterProductId;

    private Integer categoryId;

    private String categoryName;

    private String masterProductName;

    private String photo;

    private String thumbnail;

    /**
     * Vegetarian status.
     * true  = Veg
     * false = Non-Veg
     */
    private Boolean isVeg;

    /**
     * Product type stored in master_products.product_type.
     */
    private String productType;
}