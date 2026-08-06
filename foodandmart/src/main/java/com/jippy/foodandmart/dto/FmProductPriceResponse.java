package com.jippy.foodandmart.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmProductPriceResponse {

    private Integer productId;
    private String productName;
    private Integer variantId;
    private String variantName;
    private BigDecimal merchantPrice;
    private BigDecimal onlinePrice;

}