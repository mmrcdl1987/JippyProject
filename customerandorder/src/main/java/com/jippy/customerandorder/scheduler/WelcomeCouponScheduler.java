package com.jippy.customerandorder.scheduler;

import com.jippy.customerandorder.iservice.WelcomeCouponSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WelcomeCouponScheduler {

    private final WelcomeCouponSchedulerService welcomeCouponSchedulerService;

    @Scheduled(cron = "*/30 * * * * ?")
//        @Scheduled(cron = "0 0 10 * * ?")
//@Scheduled(cron = "0 */3 * * * ?")
    public void processWelcomeCoupons() {

        log.info("WELCOME_COUPON_SCHEDULER_JOB_STARTED");

        try {

            welcomeCouponSchedulerService.processWelcomeCoupons();

            log.info("WELCOME_COUPON_SCHEDULER_JOB_COMPLETED");

        } catch (Exception ex) {

            log.error("WELCOME_COUPON_SCHEDULER_JOB_FAILED", ex);

        }
    }
}