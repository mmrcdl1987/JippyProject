package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.BannerSlotDayResponseDto;
import com.jippy.foodandmart.dto.SettlementWeekResponseDto;

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


    List<SettlementWeekResponseDto> getSettlementWeeks(
            Integer year
    );
}