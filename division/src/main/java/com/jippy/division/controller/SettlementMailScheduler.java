package com.jippy.division.controller;

import com.jippy.division.service.DivOutletWeeklySettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementMailScheduler {

    private final DivOutletWeeklySettlementService divOutletWeeklySettlementService;

//     for every minute there is email notification
//    @Scheduled(cron = "0 * * * * *")
    public void scheduleSettlementMail() {

        log.info("Running Settlement Mail Scheduler for every 1 minute");

        divOutletWeeklySettlementService.sendOutletSettlementMail();
    }
}