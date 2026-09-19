package com.jippy.foodandmart.projections;

import java.math.BigDecimal;

public interface FmProductMerchantPriceProjection {

    Integer getProductOrProductVariantOptionId();

    BigDecimal getMerchantPrice();

    String getPriceType();
}
