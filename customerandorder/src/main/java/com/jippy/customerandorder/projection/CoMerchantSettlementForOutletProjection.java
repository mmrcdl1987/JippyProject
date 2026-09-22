package com.jippy.customerandorder.projection;

import java.math.BigDecimal;

/**
 * Projection used specifically for fetching
 * merchant settlement calculation details
 * for each outlet.
 */
public interface CoMerchantSettlementForOutletProjection {

    /**
     * Outlet ID associated with the delivered orders.
     */
    Integer getOutletId();

    /**
     * Number of delivered orders for the outlet.
     */
    Long getOrderCount();

    /**
     * Total merchant amount from order items.
     */
    BigDecimal getMerchantTotalAmount();

    /**
     * Total merchant promotion discount.
     */
    BigDecimal getPromotionDeductedAmount();
}