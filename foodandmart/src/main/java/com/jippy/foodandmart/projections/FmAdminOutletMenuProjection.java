package com.jippy.foodandmart.projections;

import java.math.BigDecimal;
import java.time.LocalTime;

public interface FmAdminOutletMenuProjection {

    // =========================================================
    // OUTLET
    // =========================================================

    Integer getOutletId();

    String getOutletName();

    String getOutletEmail();

    String getOutletPhone();

    String getAlternateOutletPhone();

    Double getLatitude();

    Double getLongitude();

    Boolean getOutletAvailable();

    Boolean getOutletToggle();

    Boolean getGstApplied();

    // =========================================================
    // BANK
    // =========================================================

    String getAccountNumber();

    String getIfscCode();

    String getBankName();

    String getAccountHolderName();

    // =========================================================
    // ADDRESS
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
    // CUISINE
    // =========================================================

    Integer getCuisineTypeId();

    String getCuisineTypeName();

    // =========================================================
    // CATEGORY
    // =========================================================

    Integer getCategoryId();

    String getCategoryName();

    Boolean getCategoryAvailable();

    Boolean getCategoryToggle();

    // =========================================================
    // PRODUCT
    // =========================================================

    Integer getProductId();

    String getProductName();

    String getDescription();

    String getImageLink();

    BigDecimal getMerchantPrice();

    BigDecimal getOnlinePrice();

    Boolean getIsVeg();

    Boolean getHasProductVariants();

    Boolean getProductAvailable();

    Boolean getProductToggle();

    // =========================================================
    // VARIANT
    // =========================================================

    Integer getProductVariantId();

    Integer getVariantValueId();

    Integer getVariantGroupId();

    String getVariantName();

    String getVariantGroupName();

    String getVariantPriceType();

    BigDecimal getVariantMerchantPrice();

    BigDecimal getVariantOnlinePrice();

    // =========================================================
    // OUTLET TIMINGS
    // =========================================================

    Boolean getIsOpen();

    LocalTime getOpeningTime();

    LocalTime getClosingTime();

    String getOutletDay();

    // =========================================================
    // PRODUCT TIMINGS
    // =========================================================

    LocalTime getStartTime();

    LocalTime getEndTime();

    String getProductDay();
}