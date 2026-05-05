package com.jippy.foodandmart.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class FmProductTimingDto {
    private String day;
    private LocalTime startTime;
    private LocalTime endTime;
}