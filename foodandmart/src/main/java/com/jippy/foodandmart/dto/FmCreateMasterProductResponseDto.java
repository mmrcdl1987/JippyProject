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

    private Integer veg;

    private Integer nonVeg;

    /**
     * Product type stored in master_products.product_type.
     */
    private String productType;

    private Integer publish;
}