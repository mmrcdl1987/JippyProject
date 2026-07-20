package com.jippy.foodandmart.scheduler;

import com.jippy.foodandmart.service.BannerSlotDayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementWeekScheduler {

    private final BannerSlotDayService bannerSlotDayService;

    /**
     * Runs on the 1st day of every month at 12:20 AM.
     */
//    @Scheduled(cron = "0 20 0 1 * ?")
    @Scheduled(cron = "0 */10 * * * ?")
    public void maintainSettlementWeeks() {

        log.info("Settlement Week Scheduler Started");

        try {

            bannerSlotDayService.maintainSettlementWeeks();

            log.info("Settlement Week Scheduler Completed Successfully");

        } catch (Exception ex) {

            log.error("Error while maintaining settlement weeks.", ex);
        }
    }
}