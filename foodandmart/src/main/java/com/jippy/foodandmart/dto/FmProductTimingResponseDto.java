package com.jippy.foodandmart.dto;

import lombok.Data;

@Data
public class FmProductTimingResponseDto {

    private Integer productAvailableTimingId;

    private Integer dayOfWeekId;

    private String dayName;

    private String startTime;

    private String endTime;
}