package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FmProductVariantOptionDetailResponse {

    private Integer productVariantOptionsId;

    private Integer productVariantGroupValuesId;

    private String variantName;

    private String priceType;

    private BigDecimal variantPrice;
}