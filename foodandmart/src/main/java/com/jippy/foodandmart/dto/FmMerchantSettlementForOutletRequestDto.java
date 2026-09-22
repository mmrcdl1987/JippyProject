package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for fetching merchant settlement details
 * for a specific outlet and settlement period.
 *
 * Settlement period can be provided using either:
 *
 * 1. startDate + endDate
 *
 * OR
 *
 * 2. weekSlotDaysId
 */
@Getter
@Setter
public class FmMerchantSettlementForOutletRequestDto {

    /**
     * Outlet ID for which settlement details are required.
     */
    @Schema(
            description = "Outlet ID for which settlement is required",
            example = "3"
    )
    private Integer outletId;

    /**
     * Custom settlement start date.
     */
    @Schema(
            description = "Settlement start date. Use together with endDate when weekSlotDaysId is not provided.",
            example = "2026-09-01",
            nullable = true
    )
    private LocalDate startDate;

    /**
     * Custom settlement end date.
     */
    @Schema(
            description = "Settlement end date. Use together with startDate when weekSlotDaysId is not provided.",
            example = "2026-09-17",
            nullable = true
    )
    private LocalDate endDate;

    /**
     * Settlement week slot ID.
     *
     * When provided, FM resolves the corresponding
     * startDate and endDate from the settlement week slot.
     */
    @Schema(
            description = "Settlement week slot ID. Use instead of startDate and endDate.",
            example = "26",
            nullable = true
    )
    private Integer weekSlotDaysId;
}