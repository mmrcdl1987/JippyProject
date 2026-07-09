package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.BannerSlotDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BannerSlotDayRepository extends JpaRepository<BannerSlotDay, Integer> {

    Optional<BannerSlotDay> findTopByOrderBySlotEndDateDesc();
    boolean existsByBannerSlotDaysId(Integer bannerSlotDaysId);

}