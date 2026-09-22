package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Response DTO for merchant settlement summary.
 *
 * Contains the settlement calculation details
 * for each outlet within the requested settlement period.
 */
@Data
public class CoMerchantSettlementSummaryDto {

    /**
     * Outlet associated with the settlement.
     */
    private Integer outletId;

    /**
     * Total number of delivered orders
     * for the outlet.
     */
    private Long orderCount;

    /**
     * Total merchant amount calculated
     * from order_items.merchant_total_price.
     */
    private BigDecimal merchantTotalAmount;

    /**
     * Total merchant promotion discount
     * calculated from order_price_breakup.
     */
    private BigDecimal promotionDeductedAmount;
}