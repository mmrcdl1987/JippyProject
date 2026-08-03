package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmMealTypeTimingResponse {

    private Integer mealTypeTimingsId;
    private String mealType;
    private LocalTime fromTime;
    private LocalTime toTime;

}