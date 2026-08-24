package com.jippy.foodandmart.projections;

import java.math.BigDecimal;
import java.time.LocalTime;

public interface FmMerchantOutletMenuProjection {

    // =========================================================
    // OUTLET DETAILS
    // From: jippy_fm.outlets
    // =========================================================

    Integer getOutletId();

    String getOutletName();

    String getOutletEmail();

    String getOutletPhone();

    String getAlternateOutletPhone();

    // =========================================================
    // LOCATION
    // From: jippy_fm.outlets
    // =========================================================

    Double getLatitude();

    Double getLongitude();

    Boolean getOutletAvailable();

    // =========================================================
    // OUTLET BANK DETAILS
    // From: jippy_fm.user_bank_details
    // =========================================================

    String getAccountNumber();

    String getIfscCode();

    String getBankName();

    String getAccountHolderName();

    // =========================================================
    // ADDRESS DETAILS
    // From: jippy_fm.address
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
    // CUISINE TYPE
    // From: jippy_fm.cuisine_types
    //
    // outlets.cuisine_type contains INTEGER[]
    //
    // Example:
    // outlets.cuisine_type = [1,2]
    //
    // cuisine_types:
    // 1 -> INDIAN
    // 2 -> CHINESE
    // =========================================================

    Integer getCuisineTypeId();

    String getCuisineTypeName();

    // =========================================================
    // CATEGORY DETAILS
    // From: jippy_fm.categories
    // =========================================================

    Integer getCategoryId();

    String getCategoryName();

    Boolean getCategoryAvailable();

    // =========================================================
    // PRODUCT DETAILS
    // From: jippy_fm.products
    // =========================================================

    Integer getProductId();

    String getProductName();

    String getDescription();

    BigDecimal getMerchantPrice();

    Boolean getIsVeg();

    String getImageLink();

    Boolean getHasProductVariants();

    Boolean getProductAvailable();

    // =========================================================
    // PRODUCT VARIANT DETAILS
    // From: jippy_fm.product_variant_options
    // =========================================================

    Integer getProductVariantId();

    BigDecimal getVariantMerchantPrice();

    String getVariantPriceType();

    // =========================================================
    // VARIANT GROUP VALUE
    // From: jippy_fm.product_variant_group_values
    // =========================================================

    Integer getVariantValueId();

    String getVariantName();

    Integer getVariantGroupId();

    // =========================================================
    // VARIANT GROUP
    // From: jippy_fm.product_variant_groups
    // =========================================================

    String getVariantGroupName();

    Integer getVariantMinSelection();

    Integer getVariantMaxSelection();

    // =========================================================
    // OUTLET TIMINGS
    // From: jippy_fm.outlet_days
    // =========================================================

    Boolean getIsOpen();

    LocalTime getOpeningTime();

    LocalTime getClosingTime();

    String getOutletDay();

    // =========================================================
    // PRODUCT TIMINGS
    // From: jippy_fm.product_available_timings
    // =========================================================

    LocalTime getStartTime();

    LocalTime getEndTime();

    String getProductDay();
}