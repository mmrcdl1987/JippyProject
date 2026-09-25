package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoOrderProfitLossDto {

    private String orderId;

    private String outletName;
    /**
     * Amount paid by customer to Jippy.
     */
    private BigDecimal revenue;

    /**
     * Revenue per order (order total - delivery charge).
     */
    private BigDecimal revenuePerOrder;

    /**
     * Total merchant amount before GST and merchant promotion.
     */
    private BigDecimal merchantTotalPrice;

    /**
     * 5% GST deducted from merchant amount.
     */
    private BigDecimal merchantGst;

    /**
     * Final amount paid to merchant.
     */
    private BigDecimal merchantPayout;

    /**
     * Driver delivery fee paid by Jippy.
     */
    private BigDecimal driverDeliveryFee;

    /**
     * Original discount amount.
     */
    private BigDecimal discountAmount;

    /**
     * Discount type.
     */
    private String discountType;

    /**
     * Discount amount borne by Jippy.
     */
    private BigDecimal ourDiscountCost;

    /**
     * Merchant promotion discount deducted from merchant payout.
     */
    private BigDecimal merchantPromotionDiscount;

    /**
     * Revenue - total Jippy costs.
     */
    private BigDecimal profitOrLoss;

    /**
     * PROFIT / LOSS / BREAK_EVEN.
     */
    private String profitLossStatus;
}