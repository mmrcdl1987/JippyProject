package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmCurrentMealTypeResponse;
import com.jippy.foodandmart.entity.MealTypeTiming;

import java.util.List;

public interface IFmMealReminderService {

    FmCurrentMealTypeResponse getCurrentMealType();

    List<MealTypeTiming> getAllMealTypeTimings();


}