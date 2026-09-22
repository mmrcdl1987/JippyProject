package com.jippy.customerandorder.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for merchant settlement calculation
 * for selected outlets and settlement period.
 */
@Data
public class CoMerchantSettlementBetweenDatesRequestDto {

    /**
     * List of outlet IDs for which settlement is required.
     */
    private List<Integer> outletIds;

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
     * When provided, CO fetches the settlement dates
     * from FM.
     */
    private Integer weekSlotDaysId;
}