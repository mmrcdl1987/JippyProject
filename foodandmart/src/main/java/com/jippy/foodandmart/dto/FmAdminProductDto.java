package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class FmAdminProductDto {

    private Integer productId;

    private String productName;

    private String description;

    private String imageLink;

    private Boolean isVeg;

    private Boolean hasProductVariants;

    // =========================================================
    // PRODUCT STATUS
    // =========================================================

    private Boolean isAvailable;

    private Boolean isToggle;

    // =========================================================
    // PRODUCT PRICING
    // =========================================================

    private BigDecimal merchantPrice;

    private BigDecimal onlinePrice;

    // =========================================================
    // PRODUCT TIMINGS
    // =========================================================

    private List<FmProductTimingDto> productTimings = new ArrayList<>();

    // =========================================================
    // PRODUCT VARIANTS
    // =========================================================

    private List<FmAdminProductVariantDto> variants = new ArrayList<>();
}