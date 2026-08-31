package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FmPublicProductDto {

    private Integer productId;
    private String productName;
    private String description;
    private String imageLink;
    private BigDecimal merchantPrice;
    private BigDecimal onlinePrice;
    private Boolean isVeg;
    private Boolean hasProductVariants;
    private Boolean isAvailable;
    private Boolean isProductFavourite;
    private FmPublicActiveDiscountsDto activeDiscountsDto;

    @Schema(description = "True when the product has variants.")
    private Boolean variants;

    @Schema(description = "True when the product has timing entries.")
    private Boolean productTimings;
}
