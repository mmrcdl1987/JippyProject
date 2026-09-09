package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FmCurrentOnlinePriceItemRequest {

    @NotNull(message = "Product id is required")
    private Integer productId;

    /*
     * NULL  -> Product without variant
     * VALUE -> Product variant option id
     */
    private Integer variantOptionId;
}