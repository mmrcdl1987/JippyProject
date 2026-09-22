package com.jippy.customerandorder.projection;

import java.math.BigDecimal;

/**
 * Projection used to fetch all required settlement
 * calculation details for the supplied order IDs.
 *
 * The query calculates:
 * 1. Total merchant price from order_items
 * 2. Total merchant promotion discount
 */
public interface CoMerchantSettlementProjection {

    /**
     * Total merchant price of all order items.
     */
    BigDecimal getMerchantTotalPrice();

    /**
     * Total discount given through merchant promotion.
     */
    BigDecimal getPromotionDiscount();
}