package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmMealTypeTimingResponse;
import com.jippy.foodandmart.entity.MealTypeTiming;
import com.jippy.foodandmart.repository.MealTypeTimingRepository;
import com.jippy.foodandmart.repository.MealTypeTimingRepository;
import com.jippy.foodandmart.service.FmMealTypeTimingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmMealTypeTimingServiceImpl implements FmMealTypeTimingService {

    private final MealTypeTimingRepository mealTypeTimingRepository;

    @Override
    public List<FmMealTypeTimingResponse> getAllMealTypeTimings() {

        log.info("Fetching all meal type timings.");

        List<MealTypeTiming> mealTypeTimings = mealTypeTimingRepository.findAll();

        log.info("Fetched {} meal type timing records.", mealTypeTimings.size());

        return mealTypeTimings.stream()
                .map(meal -> FmMealTypeTimingResponse.builder()
                        .mealTypeTimingsId(meal.getMealTypeTimingsId())
                        .mealType(meal.getMealType())
                        .fromTime(meal.getFromTime())
                        .toTime(meal.getToTime())
                        .build())
                .toList();
    }
}