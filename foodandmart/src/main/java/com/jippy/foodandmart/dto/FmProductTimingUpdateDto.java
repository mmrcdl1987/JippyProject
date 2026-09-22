package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

@Data
public class FmProductTimingUpdateDto {

    @Schema(
            description = "Product available timing ID",
            example = "39",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer productAvailableTimingId;

    @Schema(
            description = "Updated product start time",
            example = "18:30:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalTime startTime;

    @Schema(
            description = "Updated product end time",
            example = "22:30:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalTime endTime;
}