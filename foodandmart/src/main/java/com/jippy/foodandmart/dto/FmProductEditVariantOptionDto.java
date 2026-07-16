package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmProductEditVariantOptionDto {

    private Integer productVariantOptionsId;

    private Integer productVariantGroupValuesId;

    private String variantName;

    private String priceType;

    private BigDecimal variantPrice;
}