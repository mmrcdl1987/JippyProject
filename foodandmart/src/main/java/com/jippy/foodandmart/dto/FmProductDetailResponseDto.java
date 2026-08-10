package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class FmProductDetailResponseDto {

    private Integer productId;

    private Integer outletCategoryId;

    private String productName;

    private String description;

    /**
     * products.merchant_price
     */
    private BigDecimal merchantPrice;

    /**
     * Base product online price.
     */
    private BigDecimal onlinePrice;

    private Boolean available;

    private Boolean isVeg;

    private Boolean hasProductVariants;

    private String imageLink;

    private String photos;

    private String thumbnail;

    /**
     * MAIN and ADD variants.
     */
    private List<FmProductVariantDTO> variants;
}