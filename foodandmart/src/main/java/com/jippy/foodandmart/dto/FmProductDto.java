package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmProductDto {

    @Schema(
            example = "12345",
            description = "Unique identifier for the product."
    )
    private Integer productId;

    @Schema(
            example = "Chicken Biryani",
            description = "Name of the product."
    )
    private String productName;

    @Schema(
            example = "A delicious chicken biryani.",
            description = "Description of the product."
    )
    private String description;

    // Product image URL from products.image_link
    @Schema(
            example = "https://images.unsplash.com/photo-1544145945-f90425340c7e",
            description = "Product image URL"
    )
    private String imageLink;

    @Schema(
            example = "220.00",
            description = "Price of the product for merchants."
    )
    private BigDecimal merchantPrice;

    @Schema(
            example = "249.00",
            description = "Price of the product for online customers."
    )
    private BigDecimal onlinePrice;

    @Schema(
            example = "true",
            description = "Indicates whether this product is vegetarian."
    )
    private Boolean isVeg;

    /**
     * Indicates whether the product has variants.
     *
     * true  -> variants list will be populated.
     * false -> variants list may be empty.
     */
    @Schema(
            example = "true",
            description = "Indicates whether this product has variants."
    )
    private Boolean hasProductVariants;

    // Outlet Category Toggle
    @Schema(
            example = "true",
            description = "Indicates whether the product is available."
    )
    private Boolean isAvailable;

    // Product favourite status for logged-in customer
    @Schema(
            example = "true",
            description = "Indicates whether this product is marked as favourite by the customer."
    )
    private Boolean isProductFavourite;

    /**
     * Product type stored in:
     *
     * jippy_fm.products.product_type
     *
     * Examples:
     * FOOD
     * BEVERAGE
     * GROCERY
     * DESSERT
     * RICE
     * CURRY
     * BREAKFAST
     * NOODLES
     */
    @Schema(
            example = "FOOD",
            description = "Product type stored in products.product_type."
    )
    private String productType;

    // Active discounts applicable to this product
    @Schema(
            description = "Details of active discounts applicable to this product."
    )
    private FmActiveDiscountsDto activeDiscountsDto;

    /**
     * Product variants.
     */
    @Schema(
            description = "List of product variants."
    )
    private List<FmProductVariantDTO> variants;

    /**
     * Product availability timings.
     */
    @Schema(
            description = "List of product timings indicating when the product is available."
    )
    private List<FmProductTimingDto> productTimings;
}