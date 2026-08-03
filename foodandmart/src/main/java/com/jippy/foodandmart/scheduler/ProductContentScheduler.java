package com.jippy.foodandmart.scheduler;

import com.jippy.foodandmart.service.ProductContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductContentScheduler {

    private final ProductContentService productContentService;

    /**
     * Runs every Monday at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 ? * MON")

//    @Scheduled(cron = "0 */2 * * * ?")
    public void updateProductContent() {

        log.info("========== Product Content Scheduler Started ==========");

        try {

            productContentService.processProductContent();

            log.info("========== Product Content Scheduler Completed ==========");

        } catch (Exception ex) {

            log.error("Product Content Scheduler Failed", ex);

        }

    }

}