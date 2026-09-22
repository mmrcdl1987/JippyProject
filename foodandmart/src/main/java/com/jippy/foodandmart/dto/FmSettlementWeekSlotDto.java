package com.jippy.foodandmart.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Response containing settlement week slot dates.
 */
@Data
public class FmSettlementWeekSlotDto {

    private Integer weekSlotDaysId;

    private LocalDate slotStartDate;

    private LocalDate slotEndDate;

    private String slotType;
}