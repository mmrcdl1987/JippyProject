package com.jippy.foodandmart.projections;

public interface FmOutletByMerchantProjection {

    Integer getOutletId();
    String getOutletName();
    String getOutletPhone();


    Boolean getIsApproved();

    String getStateName();
    String getCityName();
    String getAreaName();
}
