package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO received by FM from CO through Feign
 * for merchant settlement order-item details.
 *
 * This DTO contains the order-related information
 * required by FM to calculate the final settlement.
 *
 * CO provides:
 * - orderId
 * - createdAt
 * - productId
 * - variantOptionId
 * - quantity
 * - merchantTotalPrice
 * - promotionDeductedAmount
 * - discountType
 *
 * FM then enriches this information with:
 * - productName
 * - variantName
 * - GST applicability
 * - GST percentage
 */
@Getter
@Setter
public class FmMerchantSettlementOrderDetailsDto {

    /**
     * Unique order ID received from CO.
     */
    private String orderId;

    /**
     * Order creation timestamp received from CO.
     */

    private LocalDate createdAt;

    /**
     * Product ID received from CO.
     *
     * Used by FM to fetch product details.
     */
    private Integer productId;

    /**
     * Variant option ID received from CO.
     *
     * Used by FM to fetch variant details.
     */
    private Integer variantOptionId;

    /**
     * Quantity ordered.
     */
    private Integer quantity;

    /**
     * Merchant total price before promotion deduction.
     */
    private BigDecimal merchantTotalPrice;

    /**
     * Merchant promotion amount deducted from
     * the merchant total price.
     */
    private BigDecimal promotionDeductedAmount;

    /**
     * Discount type received from CO.
     *
     * Example:
     * MERCHANT_PROMOTION
     */
    private String discountType;
}