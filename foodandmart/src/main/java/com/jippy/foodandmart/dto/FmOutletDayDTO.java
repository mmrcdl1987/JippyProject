package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmOutletDayDTO {

    @NotNull(message = "Day of week ID is required")
    private Integer dayOfWeekId;

    @Builder.Default private Boolean isOpen = true;

    // HH:mm format e.g. "09:00"
    private String openingTime;

    private String closingTime;

    // "morning" or "evening" — optional, used when outlet has two slots per day
    private String slotType;
}
