package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.BannerSlotDay;
import com.jippy.foodandmart.projections.FmSettlementWeekSlotProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
//    ========================================================================================
//    ========================================================================================
    /**
     * Fetches the settlement week slot for the supplied ID.
     *
     * slot_type comparison is case-insensitive.
     */
    @Query(value = """
        /*
         * Fetch settlement week dates from week_slot_days.
         *
         * 1. Find the record using week_slot_days_id.
         * 2. Only SETTLEMENT_WEEK records are considered.
         * 3. slot_type comparison is case-insensitive.
         * 4. Return slot_start_date, slot_end_date and slot_type.
         */
        SELECT
            slot_start_date AS slotStartDate,
            slot_end_date AS slotEndDate,
            slot_type AS slotType

        FROM jippy_fm.week_slot_days

        WHERE week_slot_days_id = :weekSlotDaysId

          AND UPPER(slot_type) = 'SETTLEMENT_WEEK'

        """, nativeQuery = true)
    Optional<FmSettlementWeekSlotProjection> findSettlementWeekSlot(
            @Param("weekSlotDaysId") Integer weekSlotDaysId
    );
}