
        package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FmMasterProductResponseDto {

    private Integer masterProductId;

    private String masterProductName;

    private Integer categoryId;

    private String categoryName;

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

