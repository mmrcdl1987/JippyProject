package com.jippy.foodandmart.projections;

/**
 * Projection Interface
 *
 * Used to fetch all Outlet Pending Approval Details
 * in a single database query.
 */
public interface FmOutletLevel1PendingApprovalProjection {

    /*-------------------------------------------------------
     * Approval Request Details
     *-------------------------------------------------------*/

    Integer getApprovalRequestId();

    String getEntityType();

    Integer getEntityId();

    String getCurrentLevel();

    String getStatus();


    /*-------------------------------------------------------
     * Outlet Details
     *-------------------------------------------------------*/

    Integer getOutletId();

    String getOutletName();

    String getMerchantName();

    String getCuisineType();

    String getOutletPhone();

    String getOutletEmail();

    Boolean getOutletApproved();

    Double getLatitude();

    Double getLongitude();


    /*-------------------------------------------------------
     * Outlet KYC
     *-------------------------------------------------------*/

    String getFssaiNumber();

    String getGstNumber();


    /*-------------------------------------------------------
     * Address
     *-------------------------------------------------------*/

    Integer getAddressId();

    String getBuildingNumber();

    String getRoad();

    String getLandmark();

    String getStateName();

    String getCityName();

    String getAreaName();
}