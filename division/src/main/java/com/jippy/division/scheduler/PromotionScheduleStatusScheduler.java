package com.jippy.division.scheduler;

import com.jippy.division.service.PromotionScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionScheduleStatusScheduler {

    private final PromotionScheduleService promotionScheduleService;

    /**
     * Executes every 10 minutes on the 0th second to process schedule status transitions.
     * Transitions PENDING -> ACTIVE, PENDING -> EXPIRED, and ACTIVE -> EXPIRED.
     * Never selects or modifies CANCELLED or EXPIRED schedules.
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void processScheduleStatusTransitions() {
        log.info("[PROMOTION-SCHEDULE-SCHEDULER] Running promotion schedule status transition scheduler");
        promotionScheduleService.updateScheduleStatuses();
    }
}
