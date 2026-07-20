package com.jippy.foodandmart.config;

import com.jippy.foodandmart.service.BannerSlotDayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BannerSlotInitializer implements ApplicationRunner {

    private final BannerSlotDayService bannerSlotDayService;

    @Override
    public void run(ApplicationArguments args) {

        log.info("Generating Initial Banner Slots");

        bannerSlotDayService.generateInitialFourMonths();

        log.info("Generating Initial Settlement Weeks");

        bannerSlotDayService.generateInitialSettlementWeeks();

        log.info("Banner Slot Initialization Completed");
    }
}