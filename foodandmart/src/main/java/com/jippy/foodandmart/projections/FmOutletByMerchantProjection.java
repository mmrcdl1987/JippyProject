package com.jippy.foodandmart.projections;

public interface FmOutletByMerchantProjection {

    Integer getOutletId();
    String getOutletName();
    String getOutletPhone();

    Boolean getIsApproved();          // Outlet approval

    Boolean getMerchantApproved();    // Merchant approval

    String getStateName();
    String getCityName();
    String getAreaName();
}
