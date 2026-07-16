package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FmProductTimingRequestDto {

    private Integer productAvailableTimingId;

    @NotNull
    private Integer dayOfWeekId;

    @NotNull
    private String startTime;

    @NotNull
    private String endTime;
}