package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoMerchantPricesResponseDto {

    private Integer productId;
    private Integer productVariantOptionId;
    private BigDecimal merchantPrice;
    private String priceType;


}
