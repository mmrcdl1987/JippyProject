package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.MealTypeTiming;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealTypeTimingRepository extends JpaRepository<MealTypeTiming, Integer> {

    List<MealTypeTiming> findAllByOrderByFromTimeAsc();

    @Query("""
            SELECT m
            FROM MealTypeTiming m
            WHERE
                (
                    m.fromTime <= m.toTime
                    AND :currentTime BETWEEN m.fromTime AND m.toTime
                )
                OR
                (
                    m.fromTime > m.toTime
                    AND (
                        :currentTime >= m.fromTime
                        OR :currentTime <= m.toTime
                    )
                )
            """)
    Optional<MealTypeTiming> findCurrentMealType(LocalTime currentTime);
}