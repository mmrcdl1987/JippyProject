package com.jippy.customerandorder.projection;

import java.math.BigDecimal;

/**
 * Projection used to fetch settlement calculations
 * for all delivered orders within the selected period.
 *
 * This projection contains only order-related calculations.
 * GST calculation is handled by FM.
 */
public interface CoOutletAndOrdersSettlementProjection {

    /**
     * Number of unique outlets having delivered orders.
     */
    Integer getOutletsCountsBetweenDates();

    /**
     * Number of delivered orders.
     */
    Integer getOrderCountsBetweenDates();

    /**
     * Total merchant amount from order_items.
     */
    BigDecimal getMerchantTotalPriceBetweenDates();

    /**
     * Total merchant promotion discount.
     */
    BigDecimal getPromotionDeductedAmount();
}