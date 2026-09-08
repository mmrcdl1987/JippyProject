package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoOrderItemDetailsDto {

    private Integer productId;
    private Integer variantOptionId;
    private Integer quantity;
    private BigDecimal onlineUnitPrice;
    private BigDecimal onlinePriceTotal;
}