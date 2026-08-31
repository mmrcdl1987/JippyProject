package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FmProductUpdateResponseDto {

    private Integer productId;

    private Integer outletCategoryId;

    private String productName;

    private String description;

    private Boolean isVeg;

    private Boolean hasProductVariants;

    private BigDecimal merchantPrice;

    private String imageLink;

    private String photos;

    private String thumbnail;

    /**
     * Product type stored in:
     * jippy_fm.products.product_type
     *
     * Example:
     * PRODUCT
     * MASTERPRODUCT
     * RICE
     * CURRY
     * BREAKFAST
     * NOODLES
     * DESSERT
     * BEVERAGE
     */
    private String productType;

    private List<FmProductTimingResponseDto> timings;

    /**
     * Merchant Variant Groups with Variant Prices.
     */
    private List<FmProductEditVariantGroupDto> variantGroups;
}