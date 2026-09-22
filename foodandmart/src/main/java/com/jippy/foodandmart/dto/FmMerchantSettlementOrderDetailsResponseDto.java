package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class FmMerchantSettlementOrderDetailsResponseDto {

    @Schema(description = "Unique order ID.", example = "ORDER8")
    private String orderId;

    @Schema(
            description = "Order creation timestamp.",
            example = "2026-09-15T13:27:15"
    )
    private LocalDate createdAt;

    /**
     * Products belonging to this order.
     */
    private List<FmMerchantSettlementProductDetailsDto> products;

    /**
     * Total merchant amount for this order.
     */
    private BigDecimal merchantTotalPrice;

    /**
     * Total merchant promotion deduction for this order.
     */
    private BigDecimal promotionDeductedAmount;

    /**
     * Merchant amount after promotion deduction.
     */
    private BigDecimal amountAfterPromotion;

    /**
     * Indicates whether GST is applicable.
     */
    private Boolean gstApplied;

    /**
     * GST percentage.
     */
    private BigDecimal gstPercentage;

    /**
     * GST deducted amount.
     */
    private BigDecimal gstDeductedAmount;

    /**
     * Final amount after promotion and GST deduction.
     */
    private BigDecimal gstDeductedAmountFromPromotionAmount;

    /**
     * Final net settlement amount.
     */
    private BigDecimal netTotalSettlementsAmountAfterDeductionsBetweenDates;
}