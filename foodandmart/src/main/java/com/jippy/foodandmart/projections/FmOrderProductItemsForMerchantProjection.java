package com.jippy.foodandmart.projections;

import java.math.BigDecimal;

public interface FmOrderProductItemsForMerchantProjection {
      Integer getProductId();
     String getProductName();
     BigDecimal getProductPrice();
     String getPriceType();
     String getVariantName();
    BigDecimal getVariantPrice();
     Integer getProductVariantOptionsId();
}
