package com.jippy.foodandmart.projections;

/**
 * Projection used for fetching merchant and outlet
 * information required for merchant settlement calculation.
 */
public interface FmMerchantSettlementBetweenDatesProjection {

    /**
     * Outlet ID.
     */
    Integer getOutletId();

    /**
     * Merchant ID associated with the outlet.
     */
    Integer getMerchantId();

    /**
     * Merchant name.
     */
    String getMerchantName();

    /**
     * Merchant phone number.
     */
    String getMerchantPhone();

    /**
     * City name associated with the merchant address.
     */
    String getCityName();

    /**
     * Indicates whether GST is applicable for the outlet.
     */
    Boolean getIsGstApplied();
}