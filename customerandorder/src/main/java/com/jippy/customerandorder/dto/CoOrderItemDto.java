package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoOrderItemDto {

    @NotNull(message = "Product id is required")
    private Integer productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than zero")
    private Integer quantity;

    @NotNull(message = "Online unit price is required")
    private BigDecimal onlineUnitPrice;

    @NotNull(message = "Merchant unit price is required")
    private BigDecimal merchantUnitPrice;
}