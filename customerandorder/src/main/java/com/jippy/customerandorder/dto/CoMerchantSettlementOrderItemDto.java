package com.jippy.customerandorder.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CO DTO containing order-item settlement data.
 *
 * This DTO contains only fields owned and fetched
 * by the Customer & Order service.
 *
 * FM-specific fields such as product name, variant name,
 * GST and final settlement calculations are handled by FM.
 */
@Getter
@Setter
public class CoMerchantSettlementOrderItemDto {

    private String orderId;

    private LocalDateTime createdAt;

    private Integer productId;

    private Integer variantOptionId;

    private Integer quantity;

    private BigDecimal merchantTotalPrice;

    private BigDecimal promotionDeductedAmount;

    private String discountType;
}