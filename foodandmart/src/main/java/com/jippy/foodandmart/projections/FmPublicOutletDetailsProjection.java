package com.jippy.foodandmart.projections;

import java.math.BigDecimal;

public interface FmPublicOutletDetailsProjection {

    // OUTLET DETAILS
    Integer getOutletId();
    String getOutletName();
    Boolean getOutletAvailable();

    // CATEGORY DETAILS
    Integer getCategoryId();
    String getCategoryName();
    Boolean getCategoryAvailable();

    // PRODUCT DETAILS
    Integer getProductId();
    String getProductName();
    String getDescription();
    BigDecimal getOnlinePrice();
    Boolean getIsVeg();
    String getImageLink();
    Boolean getHasProductVariants();
    Boolean getProductAvailable();

    // PRODUCT VARIANT DETAILS
    Integer getProductVariantId();
    BigDecimal getVariantMerchantPrice();
    String getVariantPriceType();

    // VARIANT GROUP VALUE DETAILS
    Integer getVariantValueId();
    String getVariantName();
    Integer getVariantGroupId();

    // VARIANT GROUP DETAILS
    String getVariantGroupName();
    Integer getVariantMinSelection();
    Integer getVariantMaxSelection();
}
