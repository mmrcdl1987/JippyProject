package com.jippy.foodandmart.projections;

public interface FmNearbyOutletProjection {

    Integer getOutletId();

    String getOutletName();

    Double getDistanceInKm();
}