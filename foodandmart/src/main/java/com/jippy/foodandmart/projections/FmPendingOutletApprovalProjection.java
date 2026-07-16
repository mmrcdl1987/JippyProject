package com.jippy.foodandmart.projections;

import java.time.LocalDateTime;

/**
 * Projection interface for fetching pending approvals.
 *
 * Only required columns are fetched from the database
 * instead of loading the complete Outlet entity.
 */
public interface FmPendingOutletApprovalProjection {

    Integer getOutletId();

    String getOutletName();

    Integer getMerchantId();

    String getCuisineType();

    String getOutletPhone();

    String getOutletEmail();

    Boolean getIsApproved();

    LocalDateTime getCreatedAt();
}