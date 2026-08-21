package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.BannerSlotDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BannerSlotDayRepository extends JpaRepository<BannerSlotDay, Integer> {

    Optional<BannerSlotDay> findTopByOrderBySlotEndDateDesc();
    boolean existsByBannerSlotDaysId(Integer bannerSlotDaysId);

    // Banner Slot
    Optional<BannerSlotDay> findTopBySlotTypeOrderBySlotEndDateDesc(String slotType);

    boolean existsBySlotType(String slotType);

    List<BannerSlotDay>
    findBySlotTypeAndSlotStartDateBetweenOrderBySlotStartDateAsc(
            String slotType,
            LocalDate startDate,
            LocalDate endDate
    );

}