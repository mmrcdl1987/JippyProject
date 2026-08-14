package com.jippy.foodandmart.projections;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FmActivePromotionDiscountsProjection {

   Integer getPromotionPlansId();

    Integer getOutletId();

    String getPlanType();

    String getOfferName();

    String getOfferType();

    BigDecimal getOfferAmount();

    BigDecimal getMinimumOrderValue();

    Integer getProductId();

    Integer getOutletCategoryId();

    LocalDateTime getStartDateTime();

    LocalDateTime getEndDateTime();
}
