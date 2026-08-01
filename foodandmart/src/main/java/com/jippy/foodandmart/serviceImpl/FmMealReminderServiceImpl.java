package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmCurrentMealTypeResponse;
import com.jippy.foodandmart.entity.MealTypeTiming;
import com.jippy.foodandmart.service.IFmMealReminderService;
import com.jippy.foodandmart.service.MealTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmMealReminderServiceImpl implements IFmMealReminderService {

    private final MealTimeService mealTimeService;

    @Override
    public FmCurrentMealTypeResponse getCurrentMealType() {

        log.info("SERVICE_START | GET_CURRENT_MEAL_TYPE");

        try {

            MealTypeTiming mealTypeTiming = mealTimeService.getCurrentMealType();

            if (mealTypeTiming == null) {

                log.warn("No active meal type found for current time.");

                return null;
            }

            FmCurrentMealTypeResponse response =
                    FmCurrentMealTypeResponse.builder()
                            .mealTypeTimingId(mealTypeTiming.getMealTypeTimingsId())
                            .mealType(mealTypeTiming.getMealType())
                            .build();

            log.info(
                    "Current Meal Type Found | Id={} | MealType={}",
                    response.getMealTypeTimingId(),
                    response.getMealType()
            );

            log.info("SERVICE_END | GET_CURRENT_MEAL_TYPE");

            return response;

        } catch (Exception ex) {

            log.error("SERVICE_ERROR | GET_CURRENT_MEAL_TYPE", ex);

            throw ex;
        }
    }
}