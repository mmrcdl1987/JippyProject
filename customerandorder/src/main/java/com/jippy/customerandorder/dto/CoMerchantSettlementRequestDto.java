package com.jippy.customerandorder.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Request DTO for fetching merchant settlements
 * for outlets between two dates.
 *
 * Either:
 * 1. startDate and endDate
 *
 * OR:
 * 2. weekSlotDaysId
 *
 * should be provided.
 */
@Data
public class CoMerchantSettlementRequestDto {

    /**
     * Custom settlement start date.
     */
    private LocalDate startDate;

    /**
     * Custom settlement end date.
     */
    private LocalDate endDate;

    /**
     * Settlement week slot ID.
     *
     * If this is provided, startDate and endDate
     * will be taken from the week_slot_days table.
     */
    private Integer weekSlotDaysId;
}