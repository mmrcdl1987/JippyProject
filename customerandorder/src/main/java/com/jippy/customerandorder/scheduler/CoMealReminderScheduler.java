package com.jippy.customerandorder.scheduler;

import com.jippy.customerandorder.dto.CoCurrentMealTypeResponse;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.ICustomerReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoMealReminderScheduler {

    private final ICustomerReminderService customerReminderService;
    private final FMFeignClient mealReminderFeignClient;

   // @Scheduled(cron = "*/10 * * * * *")
    public void processMealReminder() {

        log.info("SCHEDULER_START | PROCESS_MEAL_REMINDER");

        try {

            log.info("Fetching current meal type from Food & Mart service.");

            CoCurrentMealTypeResponse response =
                    mealReminderFeignClient.getCurrentMealType();

            if (response == null) {
                log.warn("Current meal type response is null. Skipping scheduler execution.");
                return;
            }

            if (response.getMealType() == null || response.getMealType().isBlank()) {
                log.warn("No active meal type found. Skipping scheduler execution.");
                return;
            }

            log.info(
                    "Current Meal Type Received | Id={} | MealType={}",
                    response.getMealTypeTimingId(),
                    response.getMealType()
            );

            customerReminderService.processMealReminder(response.getMealType());

            log.info(
                    "Meal reminder scheduler completed successfully for MealType={}",
                    response.getMealType()
            );

        } catch (Exception ex) {

            log.error("SCHEDULER_ERROR | PROCESS_MEAL_REMINDER", ex);
        }

        log.info("SCHEDULER_END | PROCESS_MEAL_REMINDER");
    }
}