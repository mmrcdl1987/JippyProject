package com.jippy.foodandmart.projections;

/**
 * FM projection used to fetch product-level information
 * required for merchant settlement order details.
 *
 * This projection contains only data owned by FM.
 *
 * Data fetched:
 * - Product name
 * - Variant name
 * - GST applicability
 */
public interface FmMerchantSettlementProductDetailsProjection {

    /**
     * Product name from FM products table.
     */
    String getProductName();

    /**
     * Variant name associated with the order item.
     */
    String getVariantName();

    /**
     * Indicates whether GST is applicable for the outlet.
     */
    Boolean getGstApplied();
}