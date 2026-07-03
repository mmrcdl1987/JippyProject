package com.jippy.foodandmart.scheduler;

import com.jippy.foodandmart.service.BannerCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BannerScheduler {

    private final BannerCacheService bannerCacheService;

    @Scheduled(fixedRate = 600000)
    public void refreshBannerCache() {

        log.info("Banner Scheduler Started");

        bannerCacheService.refreshBannerCache();

    }
}