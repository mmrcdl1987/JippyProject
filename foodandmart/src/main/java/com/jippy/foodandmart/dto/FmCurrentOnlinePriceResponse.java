package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmCurrentOnlinePriceResponse {

    private Integer productId;

    private Integer variantOptionId;

    private String productName;

    private String productImage;

    private BigDecimal onlinePrice;

    private Boolean available;
}