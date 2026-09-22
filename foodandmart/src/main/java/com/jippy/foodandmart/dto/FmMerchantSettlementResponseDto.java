package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for merchant outlet settlement details.
 *
 * Contains outlet information and settlement calculation details.
 */
@Getter
@Setter
public class FmMerchantSettlementResponseDto {

    private Integer outletId;

    private String outletName;

    private String outletPhone;

    private Boolean isGstApplied;

    private LocalDateTime createdAt;

    private String buildingNumber;

    /**
     * Total merchant amount calculated from order_items.
     */
    private BigDecimal merchantTotalPrice;

    /**
     * GST amount deducted at 5% when GST is applicable.
     */
    private BigDecimal gstDeductedAmount;

    /**
     * Promotion amount deducted from merchant amount.
     */
    private BigDecimal promotionDeductedAmount;

    /**
     * Final settlement amount after GST and promotion deductions.
     */
    private BigDecimal netTotalSettlementsAmountAfterDeductions;
}