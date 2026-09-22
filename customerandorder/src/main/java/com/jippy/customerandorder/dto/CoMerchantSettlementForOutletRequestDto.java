package com.jippy.customerandorder.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Request DTO used for merchant settlement calculation
 * for a specific outlet.
 *
 * Settlement period can be provided using either:
 *
 * 1. startDate + endDate
 *
 * OR
 *
 * 2. weekSlotDaysId
 *
 * When weekSlotDaysId is provided, CO calls FM
 * to fetch the corresponding settlement dates.
 */
@Data
public class CoMerchantSettlementForOutletRequestDto {

    /**
     * Outlet ID for which settlement is required.
     */
    private Integer outletId;

    /**
     * Custom settlement period start date.
     *
     * Used when weekSlotDaysId is not provided.
     */
    private LocalDate startDate;

    /**
     * Custom settlement period end date.
     *
     * Used when weekSlotDaysId is not provided.
     */
    private LocalDate endDate;

    /**
     * Settlement week slot ID.
     *
     * When provided, CO calls FM to get
     * slotStartDate and slotEndDate.
     */
    private Integer weekSlotDaysId;
}