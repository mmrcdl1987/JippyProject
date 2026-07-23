package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class PromotionPlanRequestDto {

    @NotNull(message = "Outlet Id is required")
    private Integer outletId;

    @NotNull(message = "Promotion Plan Type Id is required")
    private Integer promotionPlanTypeId;

    @NotNull(message = "Plan Start Date is required")
    private LocalDate planStartDate;

    @NotNull(message = "Plan End Date is required")
    private LocalDate planEndDate;

    @NotNull(message = "Plan Start Time is required")
    private LocalTime planStartTime;

    @NotNull(message = "Plan End Time is required")
    private LocalTime planEndTime;

    @NotBlank(message = "Offer Name is required")
    private String offerName;

    @NotNull(message = "Minimum Order Value is required")
    private BigDecimal minimumOrderValue;

    @NotNull(message = "Offer Amount is required")
    private BigDecimal offerAmount;

    @NotBlank(message = "Offer Type is required")
    private String offerType;

    /**
     * Optional.
     * If empty and outletCategoryIds is empty,
     * promotion applies to all products.
     */
    private List<Integer> productIds;

    /**
     * Optional.
     * Applies promotion to selected categories.
     */
    private List<Integer> outletCategoryIds;
}