package com.jippy.division.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableMealSlotResponseDto {

    private Integer mealTypeTimingsId;

    private String mealType;

    private String fromTime;

    private String toTime;

    private Boolean available;

    private String message;
}