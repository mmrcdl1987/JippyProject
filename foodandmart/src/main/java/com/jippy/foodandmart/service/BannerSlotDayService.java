package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.BannerSlotDayResponseDto;

import java.util.List;

public interface BannerSlotDayService {

    // ================= Banner Slot =================

    void generateInitialFourMonths();

    void maintainBannerSlots();

    // ================= Settlement Week =================

    void generateInitialSettlementWeeks();

    void maintainSettlementWeeks();

    // ================= Common =================

    List<BannerSlotDayResponseDto> getAllSlots();
}