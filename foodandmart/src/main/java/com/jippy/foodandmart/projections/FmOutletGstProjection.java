package com.jippy.foodandmart.projections;

public interface FmOutletGstProjection {

    /**
     * Returns the outlet ID.
     */
    Integer getOutletId();

    /**
     * Returns whether GST is applicable
     * for the outlet.
     */
    Boolean getGstApplied();
}