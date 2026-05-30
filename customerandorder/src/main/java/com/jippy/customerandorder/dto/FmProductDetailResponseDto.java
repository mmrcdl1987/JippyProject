package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmProductDetailResponseDto {

    private Integer productId;
    private String productName;
    private String productImage;

    /*
     * REQUIRED FOR SCHEDULED ORDER RECALCULATION
     */
    private BigDecimal onlinePrice;

    private BigDecimal merchantPrice;

    private Boolean available;
}