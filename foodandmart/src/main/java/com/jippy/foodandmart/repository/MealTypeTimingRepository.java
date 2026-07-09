package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.MealTypeTiming;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealTypeTimingRepository extends JpaRepository<MealTypeTiming, Integer> {

    List<MealTypeTiming> findAllByOrderByFromTimeAsc();

}