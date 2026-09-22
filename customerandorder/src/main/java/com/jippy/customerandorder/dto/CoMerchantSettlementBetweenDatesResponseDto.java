package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Response DTO for merchant settlement summary
 * between the requested settlement dates.
 */
@Data
public class CoMerchantSettlementBetweenDatesResponseDto {

    /**
     * Merchant name.
     */
    private String merchantName;

    /**
     * Merchant phone number.
     */
    private String merchantPhone;

    /**
     * City name of the outlet.
     */
    private String cityName;

    /**
     * Total number of delivered orders.
     */
    private Long orderCount;

    /**
     * Total merchant amount calculated from
     * order_items.merchant_total_price.
     */
    private BigDecimal merchantTotalPrice;

    /**
     * Total promotion amount deducted from
     * order_price_breakup.discount where
     * discount_type = MERCHANT_PROMOTION.
     */
    private BigDecimal promotionDeductedAmount;

    /**
     * Indicates whether GST is applicable for the outlet.
     */
    private Boolean gstApplied;

    /**
     * GST percentage applied on merchantTotalPrice.
     */
    private BigDecimal gstPercentage;

    /**
     * GST amount deducted from merchantTotalPrice.
     */
    private BigDecimal gstDeductedAmount;

    /**
     * Final settlement amount after deducting:
     *
     * merchantTotalPrice
     * - promotionDeductedAmount
     * - gstDeductedAmount
     */
    private BigDecimal netTotalSettlementsAmountAfterDeductionsBetweenDates;
}