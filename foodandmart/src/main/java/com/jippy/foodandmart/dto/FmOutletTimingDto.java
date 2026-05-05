package com.jippy.foodandmart.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class FmOutletTimingDto {
    private String day;
    private Boolean isOpen;
    private LocalTime openingTime;
    private LocalTime closingTime;
}