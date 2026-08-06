package com.jippy.foodandmart.service;

import com.jippy.foodandmart.entity.MealTypeTiming;
import com.jippy.foodandmart.repository.MealTypeTimingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealTimeService {

    private final MealTypeTimingRepository mealTypeTimingRepository;

    // This local memory list acts as our high-speed cache
    private List<MealTypeTiming> timingsCache = new ArrayList<>();

    /**
     * Triggered automatically exactly ONCE when the application starts up.
     * Loads the static lookup data using the JPA Repository.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadFixedMealTimings() {
        try {
            // Call the repository method instead of writing raw SQL
            this.timingsCache = mealTypeTimingRepository.findAllByOrderByFromTimeAsc();
            log.info(" Successfully cached {} fixed meal timings from Repository!", timingsCache.size());
        } catch (Exception e) {
            log.error("Failed to load meal timings during startup!");
            e.printStackTrace();
        }
    }

    /**
     * Matches the current local time against the cached timings
     */
    public boolean isMealActiveNow(List<Integer> allowedMealIds) {
        if (allowedMealIds == null || allowedMealIds.isEmpty() || timingsCache.isEmpty()) {
            return false;
        }

        LocalTime now = LocalTime.now();

        return timingsCache.stream()
                .filter(t -> allowedMealIds.contains(t.getMealTypeTimingsId()))
                .anyMatch(t -> {
                    if (t.getFromTime().isBefore(t.getToTime())) {
                        // Normal day timeslot (e.g., Lunch: 11:00 to 16:00)
                        return !now.isBefore(t.getFromTime()) && !now.isAfter(t.getToTime());
                    } else {
                        // Overnight timeslot crossing midnight (e.g., Midnight Dinner: 23:00 to 02:00)
                        return !now.isBefore(t.getFromTime()) || !now.isAfter(t.getToTime());
                    }
                });
    }
    /**
     * Returns the current active meal type.
     */
    public MealTypeTiming getCurrentMealType() {

        if (timingsCache.isEmpty()) {
            return null;
        }

        LocalTime now = LocalTime.now();

        return timingsCache.stream()
                .filter(t -> {

                    if (t.getFromTime().isBefore(t.getToTime())) {

                        return !now.isBefore(t.getFromTime())
                                && !now.isAfter(t.getToTime());

                    } else {

                        return !now.isBefore(t.getFromTime())
                                || !now.isAfter(t.getToTime());
                    }

                })
                .findFirst()
                .orElse(null);
    }

}
