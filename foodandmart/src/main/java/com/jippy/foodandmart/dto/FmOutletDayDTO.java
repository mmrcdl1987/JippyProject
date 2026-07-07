package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmOutletDayDTO {


    @NotNull(message = "Day of week ID is required")
    private Integer dayOfWeekId;


    @Schema(description = "Whether outlet is open on the day", example = "true")
    @Builder.Default
    private Boolean isOpen = true;

    @Schema(description = "Outlet opening time", example = "09:00")
    // HH:mm format e.g. "09:00"
    private LocalTime openingTime;

    @Schema(description = "Outlet closing time", example = "22:00")
    private LocalTime closingTime;

    @Schema(description = "Operating slot type", example = "FULL_DAY")
    // "morning" or "evening" — optional, used when outlet has two slots per day
    private String slotType;
}
