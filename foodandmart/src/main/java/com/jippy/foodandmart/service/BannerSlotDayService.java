package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.BannerSlotDayResponseDto;
import com.jippy.foodandmart.dto.FmSettlementWeekSlotDto;
import com.jippy.foodandmart.dto.SettlementWeekResponseDto;

import java.util.List;
import java.util.Optional;

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

    /**
     * Fetch settlement week slot details by slot ID.
     */
    Optional<FmSettlementWeekSlotDto> findSettlementWeekSlot(
            Integer weekSlotDaysId);


}