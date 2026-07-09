package com.jippy.foodandmart.scheduler;

import com.jippy.foodandmart.service.BannerSlotDayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BannerSlotScheduler {

    private final BannerSlotDayService bannerSlotDayService;

    /**
     * Runs on the 1st day of every month at 12:10 AM.
     */
//    @Scheduled(cron = "0 10 0 1 * ?")
    @Scheduled(cron = "0 */10 * * * ?")
    public void maintainBannerSlots() {

        log.info("Banner Slot Scheduler started.");

        try {
            bannerSlotDayService.maintainBannerSlots();
            log.info("Banner Slot Scheduler completed successfully.");
        } catch (Exception ex) {
            log.error("Error while maintaining banner slots.", ex);
        }
    }
}