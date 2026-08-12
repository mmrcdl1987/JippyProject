package com.jippy.foodandmart.scheduler;

import com.jippy.foodandmart.service.IFmScheduledPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FmProductPriceScheduler {

    private final IFmScheduledPriceService scheduledPriceService;

    /**
     * Applies scheduled prices and restores expired prices.
     * <p>
     * Runs every 5 minutes.
     * <p>
     * Change the cron after testing if required.
     */
    // @Scheduled(cron = "0 */2 * * * *")
   @Scheduled(cron = "0 0 1 * * ?")
    public void processScheduledPrices() {

        log.info("PRODUCT_PRICE_SCHEDULER_START");

        try {

            scheduledPriceService.applyScheduledPrices();

            scheduledPriceService.restoreExpiredPrices();

            log.info("PRODUCT_PRICE_SCHEDULER_COMPLETED");

        } catch (Exception exception) {

            log.error("PRODUCT_PRICE_SCHEDULER_FAILED", exception);
        }
    }
}
