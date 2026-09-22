package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoMerchantSettlementsForOutletsResponseDto {

    private String merchantName;

    private String merchantMobileNumber;

    private String cityName;

    private Long totalOrderCount;

    private BigDecimal merchantTotalPrice;

    private BigDecimal promotionDeductedAmount;

    private Boolean gstApplied;

    private BigDecimal gstPercentage;

    private BigDecimal gstDeductedAmount;

    private BigDecimal netTotalSettlementAmountAfterDeductions;
}