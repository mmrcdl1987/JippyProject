package com.jippy.foodandmart.projections;

/**
 * ===========================================================
 * Projection Interface for Driver Level-1 Pending Approval
 * ===========================================================
 */
public interface FmDriverLevel1PendingApprovalProjection {

    /*==========================================================
     = Approval Request Details
     ==========================================================*/

    Integer getApprovalRequestId();

    String getEntityType();

    Integer getEntityId();

    String getCurrentLevel();

    String getStatus();

    /*==========================================================
     = Driver Id
     ==========================================================*/

    Integer getDriverId();
}