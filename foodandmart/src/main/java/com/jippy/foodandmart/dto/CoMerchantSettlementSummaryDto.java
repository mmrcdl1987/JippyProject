package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CoMerchantSettlementSummaryDto {

    private Integer outletId;

    private Long orderCount;

    private BigDecimal merchantTotalAmount;

    private BigDecimal promotionDeductedAmount;
}