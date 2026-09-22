package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FmMerchantSettlementBetweenDatesResponseDto {

    private Integer merchantId;

    private String merchantName;

    private String merchantPhone;

    private String cityName;

    private Long orderCount;

    private Long outletCount;

    private BigDecimal merchantTotalPrice;

    private BigDecimal promotionDeductedAmount;

    private BigDecimal amountAfterPromotion;

    private Boolean gstApplied;

    private BigDecimal gstPercentage;

    private BigDecimal gstDeductedAmount;

    private BigDecimal gstDeductedAmountFromPromotionAmount;

    private BigDecimal netTotalSettlementsAmountAfterDeductionsBetweenDates;
}