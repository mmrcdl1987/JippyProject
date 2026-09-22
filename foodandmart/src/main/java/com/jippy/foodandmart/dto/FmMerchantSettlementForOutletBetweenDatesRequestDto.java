package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for fetching merchant settlement details
 * for a specific outlet within the requested settlement period.
 *
 * Settlement period can be provided using:
 * 1. startDate and endDate
 * OR
 * 2. weekSlotDaysId
 */
@Getter
@Setter
public class FmMerchantSettlementForOutletBetweenDatesRequestDto {

    /**
     * Outlet ID for which settlement is required.
     */
    @Schema(
            description = "Outlet ID for which settlement is required.",
            example = "3"
    )
    private Integer outletId;

    /**
     * Custom settlement start date.
     */
    @Schema(
            description = "Custom settlement start date. Use this together with endDate when weekSlotDaysId is not provided.",
            example = "2026-09-01",
            nullable = true
    )
    private LocalDate startDate;

    /**
     * Custom settlement end date.
     */
    @Schema(
            description = "Custom settlement end date. Use this together with startDate when weekSlotDaysId is not provided.",
            example = "2026-09-17",
            nullable = true
    )
    private LocalDate endDate;

    /**
     * Settlement week slot ID.
     *
     * When provided, the corresponding start date
     * and end date are fetched from FM.
     */
    @Schema(
            description = "Settlement week slot ID. When provided, the corresponding start date and end date are fetched from FM.",
            example = "26",
            nullable = true
    )
    private Integer weekSlotDaysId;
}