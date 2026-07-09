package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.BannerSlotDayResponseDto;

import java.util.List;

public interface BannerSlotDayService {

    void generateInitialFourMonths();

    void maintainBannerSlots();

    List<BannerSlotDayResponseDto> getAllSlots();

}