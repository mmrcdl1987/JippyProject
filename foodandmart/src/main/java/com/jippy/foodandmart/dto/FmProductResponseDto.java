package com.jippy.foodandmart.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FmProductResponseDto {

//    used  product Id and ProductName for Co Merchant Settlement API response,
//    contains product details along with merchant price and online price (if available)
    private Integer productId;
    private String productName;

    private BigDecimal merchantPrice;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal onlinePrice;
}