package com.jippy.foodandmart.projections;

/**
 * ===========================================================
 * Projection Interface for Merchant Level-1 Pending Approval
 * ===========================================================
 *
 * Fetches Merchant Approval Request Details using Native Query.
 */
public interface FmMerchantLevel1PendingApprovalProjection {

    /*==========================================================
     = Approval Request Details
     ==========================================================*/

    Integer getApprovalRequestId();

    String getEntityType();

    Integer getEntityId();

    String getCurrentLevel();

    String getStatus();

    /*==========================================================
     = Merchant Details
     ==========================================================*/

    Integer getMerchantId();

    String getMerchantName();

    String getMerchantEmail();

    String getMerchantPhone();

    String getMerchantBusinessType();

    Boolean getMerchantApproved();

    String getMerchantProfilePicUrl();

    /*==========================================================
     = Merchant KYC Details
     ==========================================================*/

    String getAadhaarNumber();

    String getPanNumber();

    /*==========================================================
     = Address Details
     ==========================================================*/

    Integer getAddressId();

    String getBuildingNumber();

    String getRoad();

    String getLandmark();

    /*==========================================================
     = State / City / Area
     ==========================================================*/

    String getStateName();

    String getCityName();

    String getAreaName();
}