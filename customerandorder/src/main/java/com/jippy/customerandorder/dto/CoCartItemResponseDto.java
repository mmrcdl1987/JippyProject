package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoCartItemResponseDto {

    private Integer productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal totalPrice;
}