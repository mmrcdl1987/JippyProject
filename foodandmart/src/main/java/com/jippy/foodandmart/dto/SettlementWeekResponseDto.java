package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SettlementWeekResponseDto {

    private String weekName;

    private LocalDate startDate;

    private LocalDate endDate;
}