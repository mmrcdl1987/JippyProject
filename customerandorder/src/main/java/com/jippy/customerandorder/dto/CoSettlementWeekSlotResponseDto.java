package com.jippy.customerandorder.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CoSettlementWeekSlotResponseDto {

    private LocalDate slotStartDate;

    private LocalDate slotEndDate;

    private String slotType;
}