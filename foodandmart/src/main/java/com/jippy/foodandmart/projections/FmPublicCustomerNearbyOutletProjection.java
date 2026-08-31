package com.jippy.foodandmart.projections;

import java.math.BigDecimal;

public interface FmPublicCustomerNearbyOutletProjection {

    Integer getOutletId();

    String getOutletName();

    Integer getMerchantId();

    BigDecimal getRating();

    Boolean getIsActive();

    Boolean getIsApproved();

    Double getDistanceKm();

    String getRoadDistance();

    Boolean getOpenNow();

    Boolean getIsVegOutlet();

    String getOutletPicUrl();
}
