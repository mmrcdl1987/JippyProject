package com.jippy.foodandmart.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutletProductPricingDto {

    private Integer productId;

    private String productName;

    private BigDecimal merchantPrice;

    private BigDecimal onlinePrice;
}