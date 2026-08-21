package com.jippy.foodandmart.projections;

import java.math.BigDecimal;
import java.time.LocalTime;

// 1)projection interface to ---fetch the menu details of an outlet, including outlet information,
// -category information, product information, and their availability based on days and timings.
// 2)projection interface to hold the data coming
// -from JOIN query (Outlet + Category + Product + OutletDays + ProductDays)
// 3)projection -- for Native Query Mapping--JOINS

public interface FmOutletMenuProjection {
    // =========================================================
    // for outlet table
    // =========================================================

    Integer getOutletId();
    String getOutletName();
    String getOutletPhone();
    String getOutletEmail();
    String getAlternateOutletPhone();


//    ---------------------------------------------------------
    Integer[] getCuisineType();
    Double getLatitude();
    Double getLongitude();
    // Outlet is_available
    Boolean getOutletAvailable();
    // =========================================================
    // Outlet bank details (user_bank_details table)
    // =========================================================
    String getAccountNumber();

    String getIfscCode();

    String getBankName();

    String getAccountHolderName();

    // =========================================================
    // Address
    // =========================================================

    String getBuildingNumber();

    String getRoad();

    String getLandmark();

    Integer getCityId();

    String getCityName();

    Integer getStateId();

    String getStateName();

    Integer getAreaId();

    String getAreaName();

    // =========================================================
    // Product Online Pricing
    // =========================================================

    /**
     * Product-level customer price.
         *
     * For products with variants:
     * minimum online price among active/approved variants.
     */
    BigDecimal getOnlinePrice();

    // =========================================================
    // Category
    // =========================================================
    Integer getCategoryId();
    String getCategoryName();
    // Category is_available
    Boolean getCategoryAvailable();


    // =========================================================
    // Product Details
    // =========================================================
    Integer getProductId();
    String getProductName();
    String getDescription();
    BigDecimal getMerchantPrice();
    Boolean getIsVeg();
    String getImageLink();
    Boolean getHasProductVariants();
    // Product is_available
    Boolean getProductAvailable();
    // =========================================================
    // Product Variant Details
    // =========================================================

    /**
     * product_variant_options.product_variant_options_id
     */
    Integer getProductVariantId();

    /**
     * product_variant_group_values.product_variant_group_values_id
     */
//    Integer getVariantValueId();

    /**
     * product_variant_group_values.product_variant_groups_id
     */
//    Integer getVariantGroupId();

    /**
     * product_variant_group_values.variant_name
     */
    String getVariantName();

    /**
     * product_variant_groups.group_name
     */
    String getVariantGroupName();

    /**
     * product_variant_groups.selection_type
     */
//    String getVariantSelectionType();

    /**
     * product_variant_groups.min_selection
     */
//    Integer getVariantMinSelection();

    /**
     * product_variant_groups.max_selection
     */
//    Integer getVariantMaxSelection();

    /**
     * product_variant_options.price_type
     */
    String getVariantPriceType();

    /**
     * Merchant price from product_variant_options.variant_price.
     */
    BigDecimal getVariantMerchantPrice();

    /**
     * Customer price from product_online_pricing.online_price,
     * matched using product_variant_id.
     */
    BigDecimal getVariantOnlinePrice();
    // =========================================================
    // Outlet Timings --for outlet_days table
    // =========================================================
    Boolean getIsOpen();
    LocalTime getOpeningTime();
    LocalTime getClosingTime();
    String getOutletDay();

    // =========================================================
    // Product Timings --for product_day table
    // =========================================================

    LocalTime getStartTime();
    LocalTime getEndTime();
    String getProductDay();

}


