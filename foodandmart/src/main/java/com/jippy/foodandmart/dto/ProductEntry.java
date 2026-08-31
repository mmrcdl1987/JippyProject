package com.jippy.foodandmart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductEntry {

    /**
     * Master product ID.
     *
     * This is the primary field used by the backend
     * to fetch the master product.
     */
    private Integer masterProductId;

    /**
     * Product name.
     *
     * Kept for mobile/bulk request compatibility.
     *
     * During master-product mapping, the backend uses
     * master_products.master_product_name.
     */
    @NotBlank(message = "Product name is required")
    private String productName;

    /**
     * Product description.
     *
     * During master-product mapping, the backend uses
     * the description from master_products.
     */
    private String description;

    /**
     * Category ID of the master product.
     *
     * Required for bulk mapping when products belong
     * to different categories.
     */
    private Integer categoryId;

    /**
     * Product type from master_products.product_type.
     *
     * Examples:
     *
     * RICE
     * CURRY
     * BREAKFAST
     * NOODLES
     * DESSERT
     * BEVERAGE
     */
    private String productType;

    /**
     * Veg / Non-Veg.
     *
     * Kept for mobile compatibility.
     *
     * Master product value will be used during mapping.
     */
    private Boolean isVeg;

    /**
     * Product variants.
     *
     * NOT USED while mapping master products.
     *
     * Kept so existing mobile requests do not break.
     */
    private Boolean hasProductVariants = false;

    /**
     * Merchant price supplied by UI / CSV.
     *
     * Used for the outlet product.
     */
    private BigDecimal merchantPrice;

    /**
     * Product image.
     *
     * Master product image is preferred during mapping.
     */
    private String imageLink;

    /**
     * CSV timing.
     */
    private String csvTiming;

    /**
     * CSV day of week.
     */
    private String csvDayOfWeek;

    /**
     * Explicit product timings.
     *
     * Kept for mobile compatibility.
     */
    @Valid
    private List<FmProductTimingRequestDto> timings;

    /**
     * Variant groups.
     *
     * NOT USED during master-product mapping.
     *
     * Kept only for compatibility with existing clients.
     */
    @Valid
    private List<FmProductVariantOptionGroupDto> variantGroups;
}