package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OutletSubscriptionPlanRequestDto {

    @NotNull(message = "Outlet Id is required")
    private Integer outletId;

    @NotNull(message = "Subscription Plan Id is required")
    private Integer subscriptionPlanId;

    @NotNull(message = "Banner From Date is required")
    private LocalDate bannerFromDate;

    @NotBlank(message = "Price Model Type is required")
    private String priceModelType;

    @NotNull(message = "Offer Amount is required")
    private BigDecimal offerAmount;

    @NotNull(message = "User Id is required")
    private Integer userId;
}