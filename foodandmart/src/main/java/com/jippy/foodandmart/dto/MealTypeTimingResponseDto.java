package com.jippy.foodandmart.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class MealTypeTimingResponseDto {

    private Integer mealTypeTimingsId;
    private String mealType;
    private LocalTime fromTime;
    private LocalTime toTime;
}