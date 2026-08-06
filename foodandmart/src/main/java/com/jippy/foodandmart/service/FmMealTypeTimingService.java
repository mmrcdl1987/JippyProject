package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmMealTypeTimingResponse;
import com.jippy.foodandmart.entity.MealTypeTiming;

import java.util.List;

public interface FmMealTypeTimingService {

    List<FmMealTypeTimingResponse> getAllMealTypeTimings();

    boolean existsById(Integer mealTypeTimingsId);

}