package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmProductVariantOptionResponseDto {

    private Integer productVariantOptionsId;

    private Integer productId;

    private Integer productVariantGroupValuesId;

    private String groupName;

    private String variantName;

    private String priceType;

    private BigDecimal variantPrice;

    private Boolean isActive;
}