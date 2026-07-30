package com.jippy.foodandmart.projections;

import java.math.BigDecimal;

public interface FmNearbyOutletProjection {

    Integer getOutletId();

    String getOutletName();

    Integer getMerchantId();

    String getCuisineType();

    String getOutletPhone();

    BigDecimal getRadius();

    Double getDistanceInKm();
}