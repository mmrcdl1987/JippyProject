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
     * Primary field used by the backend
     * to fetch the master product.
     */
    private Integer masterProductId;

    /**
     * Product name.
     *
     * During master-product mapping, the backend
     * should use master_products.master_product_name.
     */
    @NotBlank(message = "Product name is required")
    private String productName;

    /**
     * Product description.
     *
     * During master-product mapping, the backend
     * should use master_products.description.
     */
    private String description;

    /**
     * Category ID of the master product.
     */
    private Integer categoryId;

    /**
     * Category name from master_products.category_name.
     */
    private String categoryName;

    /**
     * Product type from master_products.product_type.
     */
    private String productType;

    /**
     * Veg / Non-Veg.
     *
     * Master product value will be used during mapping.
     */
    private Boolean isVeg;

    /**
     * Product variants.
     *
     * Not used while mapping master products.
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
     */
    @Valid
    private List<FmProductTimingRequestDto> timings;

    /**
     * Variant groups.
     */
    @Valid
    private List<FmProductVariantOptionGroupDto> variantGroups;
}