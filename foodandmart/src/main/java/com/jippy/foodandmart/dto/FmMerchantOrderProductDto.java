package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmMerchantOrderProductDto {

    private Integer productId;
    private String productName;

    private Integer variantOptionsId;
    private String variantOptionName;

    private String productPicUrl;

    private BigDecimal merchantUnitPrice;
    private BigDecimal merchantTotalPrice;

    private Integer quantity;
}
