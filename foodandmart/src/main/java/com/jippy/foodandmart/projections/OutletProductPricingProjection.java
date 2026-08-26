package com.jippy.foodandmart.projections;


import java.math.BigDecimal;

public interface OutletProductPricingProjection {

    Integer getProductId();

    String getProductName();

    BigDecimal getMerchantPrice();

    BigDecimal getOnlinePrice();
}