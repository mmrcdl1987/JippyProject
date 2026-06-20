package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductItemDto {

    private Integer productId;
    private String productName;
    private BigDecimal onlinePrice;
    private Integer quantity;
}
