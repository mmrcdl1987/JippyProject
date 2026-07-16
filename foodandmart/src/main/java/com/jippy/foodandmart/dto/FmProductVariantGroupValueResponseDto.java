package com.jippy.foodandmart.dto;

import lombok.Data;

@Data
public class FmProductVariantGroupValueResponseDto {

    private Integer productVariantGroupValuesId;

    private Integer productVariantGroupsId;

    private String variantName;

    private Boolean isActive;
}