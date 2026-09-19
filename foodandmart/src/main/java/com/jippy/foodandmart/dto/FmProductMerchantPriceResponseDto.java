package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmProductMerchantPriceResponseDto {

    private Integer productId;
    private Integer productVariantOptionId;
    private BigDecimal merchantPrice;
    private String priceType;

}
