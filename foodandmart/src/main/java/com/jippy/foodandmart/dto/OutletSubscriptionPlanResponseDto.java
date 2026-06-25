package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OutletSubscriptionPlanResponseDto {

    private Integer outletSubscriptionPlanId;
    private Integer outletId;
    private Integer subscriptionPlanId;

    private LocalDate subscriptionFromDate;
    private LocalDate subscriptionToDate;

    private LocalDate bannerFromDate;
    private LocalDate bannerToDate;

    private String priceModelType;
    private BigDecimal offerAmount;
}