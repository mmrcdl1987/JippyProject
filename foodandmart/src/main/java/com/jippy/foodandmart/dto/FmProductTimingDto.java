package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

@Data
public class FmProductTimingDto {

    @Schema(example = "Monday", description = "Day of the week for the product timing.")
    private String day;
    @Schema(example = "true", description = "Indicates whether the product is available on this day.")
    private LocalTime startTime;
    @Schema(example = "22:00", description = "Closing time for the product on this day.")
    private LocalTime endTime;
}