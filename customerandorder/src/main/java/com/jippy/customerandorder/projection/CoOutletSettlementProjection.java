package com.jippy.customerandorder.projection;

import java.math.BigDecimal;

public interface CoOutletSettlementProjection {

    /**
     * Outlet having delivered orders
     * during the settlement period.
     */
    Integer getOutletId();

    /**
     * Total merchant amount for this outlet.
     */
    BigDecimal getMerchantTotalPrice();

    /**
     * Total merchant promotion deduction
     * for this outlet.
     */
    BigDecimal getPromotionDeductedAmount();
}