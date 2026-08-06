package com.jippy.foodandmart.projections;

import java.math.BigDecimal;

public interface FmProductPriceProjection {
    Integer getProductId();
    String getProductName();
    Integer getVariantId();
    String getVariantName();
    BigDecimal getMerchantPrice();
    BigDecimal getOnlinePrice();
}
