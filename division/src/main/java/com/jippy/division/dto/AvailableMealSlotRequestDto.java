package com.jippy.division.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AvailableMealSlotRequestDto {

    // Selected location
    private Integer locationId;

    // STATE / CITY / AREA
    private String locationType;

    // Selected outlets
    private List<Integer> outletIds;

    // Selected date range
    private LocalDate promotionFromDate;

    private LocalDate promotionToDate;
}