package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmAdminProductVariantDto {

    private Integer variantId;

    private Integer variantValueId;

    private Integer variantGroupId;

    private String variantName;

    private String groupName;

    private String priceType;

    /*
     * From product_variant_options.variant_price
     */
    private BigDecimal merchantPrice;

    /*
     * From product_online_pricing.online_price
     */
    private BigDecimal onlinePrice;
}