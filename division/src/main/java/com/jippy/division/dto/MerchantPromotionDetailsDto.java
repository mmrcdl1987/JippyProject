package com.jippy.division.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantPromotionDetailsDto {

    private Integer promotionPlanId;

    private String offerName;

    private BigDecimal offerAmount;

    private String offerType;

    private BigDecimal minimumOrderValue;
}