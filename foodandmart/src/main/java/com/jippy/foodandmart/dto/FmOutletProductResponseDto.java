package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FmOutletProductResponseDto {

    private Integer productId;

    private String productName;

    private Integer outletCategoryId;

    private String categoryName;
}