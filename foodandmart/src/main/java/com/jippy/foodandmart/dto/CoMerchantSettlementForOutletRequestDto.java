package com.jippy.foodandmart.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Request DTO sent from FM to CO
 * for calculating merchant settlement
 * for a specific outlet.
 *
 * FM resolves weekSlotDaysId into
 * startDate and endDate before calling CO.
 *
 * CO only needs:
 * - outletId
 * - startDate
 * - endDate
 */
@Data
public class CoMerchantSettlementForOutletRequestDto {

    /**
     * Outlet ID for which settlement
     * calculation is required.
     */
    private Integer outletId;

    /**
     * Settlement period start date.
     *
     * This can come directly from the
     * custom date request or can be resolved
     * from the settlement week slot.
     */
    private LocalDate startDate;

    /**
     * Settlement period end date.
     *
     * This can come directly from the
     * custom date request or can be resolved
     * from the settlement week slot.
     */
    private LocalDate endDate;
}