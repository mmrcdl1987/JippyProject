package com.jippy.foodandmart.scheduler;

import com.jippy.foodandmart.service.IFmAutoApprovalSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler responsible for automatically processing
 * eligible pending Approval Requests.
 *
 * <p>
 * The scheduler only triggers the Auto Approval process.
 * Actual business logic is handled by
 * IFmAutoApprovalSchedulerService.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FmAutoApprovalScheduler {

    /**
     * Service responsible for Auto Approval processing.
     */
    private final IFmAutoApprovalSchedulerService autoApprovalSchedulerService;

    /**
     * Executes the Auto Approval process automatically.
     *
     * <p>
     * TESTING:
     * Runs once every 1 minute.
     *
     * <p>
     * NOTE:
     * Change this schedule to the required production
     * interval after testing is completed.
     */
//    @Scheduled(fixedRate = 60000) ---- FOR TESTING RUNNING EVEY 1 MIN
//    ========================================================================================
    /**
     * Executes the Auto Approval process automatically.
     *
     * <p>
     * PRODUCTION:
     * Runs every day at 1:00 AM IST.
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Kolkata")
    public void runAutoApprovalScheduler() {
        log.info("==================================================");
        log.info("Auto Approval Scheduler Job Started");
        log.info("==================================================");

        try {

            //----------------------------------------------------------
            // Process Eligible Approval Requests
            //----------------------------------------------------------

            autoApprovalSchedulerService.processAutoApprovalRequests();

            //----------------------------------------------------------
            // Scheduler Completed Successfully
            //----------------------------------------------------------

            log.info("Auto Approval Scheduler Job Completed Successfully.");

        } catch (Exception exception) {

            //----------------------------------------------------------
            // Scheduler Execution Failed
            //----------------------------------------------------------

            log.error(
                    "Error occurred while executing Auto Approval Scheduler Job.",
                    exception);
        }

        log.info("==================================================");
    }
}