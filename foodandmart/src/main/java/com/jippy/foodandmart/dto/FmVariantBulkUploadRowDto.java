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
public class FmVariantBulkUploadRowDto {

    private Integer rowNumber;

    private String productName;

    private String variantGroupName;

    private String variantGroupValue;

    private String priceType;

    private BigDecimal variantPrice;
}