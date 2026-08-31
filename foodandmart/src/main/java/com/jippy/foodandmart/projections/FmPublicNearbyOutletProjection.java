package com.jippy.foodandmart.projections;

import java.math.BigDecimal;

public interface FmPublicNearbyOutletProjection {

    Integer getOutletId();

    String getOutletName();

    Integer getMerchantId();

    String getOutletPhone();

    BigDecimal getRadius();

    String getSubscriptionStatus();

    String getPromotionStatus();

    Double getReview();

    Boolean getIsActive();

    Boolean getIsApproved();

    Double getDistanceInKm();
}
