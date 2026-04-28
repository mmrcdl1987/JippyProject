package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmDaysOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FmDaysOfWeekRepository extends JpaRepository<FmDaysOfWeek, Integer> {
    Optional<FmDaysOfWeek> findByDayNameIgnoreCase(String dayName);
}
