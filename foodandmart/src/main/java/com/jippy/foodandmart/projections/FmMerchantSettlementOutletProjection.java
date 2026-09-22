package com.jippy.foodandmart.projections;

import java.time.LocalDateTime;

/**
 * Projection for fetching outlet details required
 * for merchant settlement calculation.
 *
 * This projection is specifically used by
 * getSettlementsForMerchant API.
 */
public interface FmMerchantSettlementOutletProjection {

    Integer getOutletId();

    String getOutletName();

    String getOutletPhone();

    Boolean getIsGstApplied();

    LocalDateTime getCreatedAt();

    String getBuildingNumber();
}