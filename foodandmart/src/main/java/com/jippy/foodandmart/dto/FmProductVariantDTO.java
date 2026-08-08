package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmProductVariantDTO {

    /**
     * product_variant_options.product_variant_options_id
     */
    private Integer variantId;

    /**
     * Variant display name.
     * Example: Small, Medium, Large, Extra Cheese.
     */
    private String variantName;

    /**
     * Source price from:
     * product_variant_options.variant_price
     */
    private BigDecimal merchantPrice;

    /**
     * Final online price from:
     * product_online_pricing.online_price
     */
    private BigDecimal price;

    /**
     * MAIN / ADD
     */
    private String priceType;
}