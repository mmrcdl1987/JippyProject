package com.jippy.customerandorder.scheduler;

import com.jippy.customerandorder.iservice.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartReminderScheduler {

    private final ICartService cartReminderService;

    @Scheduled(cron = "0 */5 * * * *")
    public void processCartReminderJob() {

        log.info("==================================================");
        log.info("CART REMINDER SCHEDULER STARTED");
        log.info("==================================================");

        try {

            cartReminderService.processCartReminders();

        } catch (Exception ex) {

            log.error("Error while processing cart reminders", ex);
        }

        log.info("==================================================");
        log.info("CART REMINDER SCHEDULER COMPLETED");
        log.info("==================================================");
    }
}