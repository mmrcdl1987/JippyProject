package com.jippy.foodandmart.projections;

import java.math.BigDecimal;

/**
 * FM projection used to fetch product, variant and GST
 * information required for merchant settlement.
 *
 * This projection contains only FM database information.
 *
 * CO already has:
 * - orderId
 * - createdAt
 * - quantity
 * - merchantTotalPrice
 * - promotionDeductedAmount
 *
 * Therefore, FM only provides:
 * - productName
 * - variantName
 * - GST applicability
 * - GST percentage
 */
public interface FmMerchantSettlementProductProjection {

    /**
     * Product ID from FM products table.
     *
     * Used by CO to match the response with
     * its order item.
     */
    Integer getProductId();

    /**
     * Variant option ID from FM variant tables.
     *
     * Used by CO to match the selected variant.
     */
    Integer getVariantOptionId();

    /**
     * Product name from FM products table.
     */
    String getProductName();

    /**
     * Variant name from FM variant tables.
     */
    String getVariantName();

    /**
     * Indicates whether GST is applicable
     * for the outlet.
     */
    Boolean getGstApplied();

    /**
     * GST percentage applicable for the outlet.
     */
    BigDecimal getGstPercentage();
}