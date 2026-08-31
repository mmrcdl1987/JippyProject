package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FmProductUpdateRequestDto {

    @NotBlank(message = "Product Name is required")
    private String productName;

    @Schema(
            description = "Outlet Category Id",
            example = "79"
    )
    @Positive(
            message = "outletCategoryId must be greater than 0 Negative Driver Id's are Not Allowed"
    )
    private Integer outletCategoryId;

    private String description;

    private Boolean isVeg;

    private Boolean hasProductVariants;

    private BigDecimal merchantPrice;

    private String imageLink;

    private String photos;

    private String thumbnail;

    /**
     * Product type stored in:
     * jippy_fm.products.product_type
     *
     * Normally this value comes from the master product
     * during master-product mapping.
     *
     * Examples:
     * PRODUCT
     * MASTERPRODUCT
     * RICE
     * CURRY
     * BREAKFAST
     * NOODLES
     * DESSERT
     * BEVERAGE
     */
    @Schema(
            description = "Product type",
            example = "RICE"
    )
    private String productType;

    @Valid
    private List<FmProductTimingRequestDto> timings;

    /**
     * Merchant Variant Groups with Variant Prices.
     *
     * Used only when hasProductVariants = true.
     */
    @Valid
    private List<FmProductVariantOptionGroupDto> variantGroups;
}