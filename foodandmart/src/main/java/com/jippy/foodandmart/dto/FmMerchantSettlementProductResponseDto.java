package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * FM response DTO containing only FM-specific
 * information required by the CO settlement flow.
 *
 * CO already has order, quantity and pricing information.
 * Therefore, FM returns only:
 *
 * - Product information
 * - Variant information
 * - GST information
 */
@Getter
@Setter
public class FmMerchantSettlementProductResponseDto {

    /**
     * Product ID used by CO to identify the product.
     */
    @Schema(
            description = "Product ID.",
            example = "10"
    )
    private Integer productId;

    /**
     * Variant option ID used by CO to identify
     * the selected product variant.
     */
    @Schema(
            description = "Product variant option ID.",
            example = "5"
    )
    private Integer variantOptionId;

    /**
     * Product name fetched from FM products table.
     */
    @Schema(
            description = "Product name.",
            example = "Butter Chicken"
    )
    private String productName;

    /**
     * Variant name fetched from FM variant tables.
     */
    @Schema(
            description = "Product variant name.",
            example = "Regular"
    )
    private String variantName;

    /**
     * Indicates whether GST is applicable
     * for the requested outlet.
     */
    @Schema(
            description = "Indicates whether GST is applied for the outlet.",
            example = "true"
    )
    private Boolean gstApplied;

    /**
     * GST percentage configured for the outlet.
     */
    @Schema(
            description = "GST percentage applicable to the settlement amount.",
            example = "5"
    )
    private BigDecimal gstPercentage;
}