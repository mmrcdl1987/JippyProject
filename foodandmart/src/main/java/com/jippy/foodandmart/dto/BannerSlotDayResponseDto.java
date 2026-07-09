package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BannerSlotDayResponseDto {

    private Integer bannerSlotDaysId;

    private LocalDate slotStartDate;

    private LocalDate slotEndDate;

}