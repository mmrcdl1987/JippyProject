package com.jippy.foodandmart.projections;

import java.time.LocalDateTime;

/**
 * Projection used to fetch rejected approval information
 * from Approval Transactions, Approval Requests,
 * Outlet and Merchant tables.
 *
 * DRIVER details are fetched separately from Driver
 * Microservice using Feign Client.
 */
public interface FmRejectedApprovalProjection {

    //----------------------------------------------------------
    // Approval Transaction
    //----------------------------------------------------------

    Integer getApprovalTransactionsId();

    String getEntityType();

    Integer getEntityId();

    String getApprovalLevel();

    String getStatus();

    String getRejectedReason();

    Integer getRejectedBy();

    LocalDateTime getRejectedAt();

    //----------------------------------------------------------
    // Approval Request
    //----------------------------------------------------------

    Integer getApprovalRequestId();

    //----------------------------------------------------------
    // Entity Details
    //----------------------------------------------------------

    String getEntityName();

    String getEmail();

    String getPhone();

    String getAlternatePhone();

    String getProfilePicUrl();

    Boolean getApproved();
}