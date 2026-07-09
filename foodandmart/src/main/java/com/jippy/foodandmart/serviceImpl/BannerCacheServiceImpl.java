package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.AreaBannerCacheDto;
import com.jippy.foodandmart.dto.CustomerBannerDto;
import com.jippy.foodandmart.mapper.CustomerBannerMapper;
import com.jippy.foodandmart.service.BannerCacheService;
import com.jippy.foodandmart.service.OutletSubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.jippy.foodandmart.dto.ActiveBannerResponseDto;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerCacheServiceImpl implements BannerCacheService {

    private final RedisTemplate<String, AreaBannerCacheDto> redisTemplate;
    private final OutletSubscriptionPlanService outletSubscriptionPlanService;


    @Override
    public void refreshBannerCache() {

        log.info("BANNER_CACHE_REFRESH_STARTED");

        // Step 1 - Fetch active banners
        List<ActiveBannerResponseDto> banners =
                outletSubscriptionPlanService.getActiveBanners();


        log.info("Total Active Banner Records : {}", banners.size());

        // Step 2 - Convert Projection -> CustomerBannerDto
        List<CustomerBannerDto> customerBanners = new ArrayList<>();

        for (ActiveBannerResponseDto banner : banners) {

            if (banner.getMainBannerUrl() != null) {

                customerBanners.add(CustomerBannerMapper.toDto(banner, "MAIN", banner.getBannerSlot(), banner.getMainBannerUrl()));
            }

            if (banner.getBestRestaurantBannerUrl() != null) {

                customerBanners.add(CustomerBannerMapper.toDto(banner, "BEST_RESTAURANT", banner.getBestRestaurantSlot(), banner.getBestRestaurantBannerUrl()));
            }

            if (banner.getDealsBannerUrl() != null) {

                customerBanners.add(CustomerBannerMapper.toDto(banner, "DEALS", banner.getDealsSlot(), banner.getDealsBannerUrl()));
            }
        }

        log.info("Customer Banner Count : {}", customerBanners.size());

        // Step 3 - Group by Area
        Map<Integer, AreaBannerCacheDto> areaBannerMap = new HashMap<>();

        for (CustomerBannerDto banner : customerBanners) {

            AreaBannerCacheDto areaBanner = areaBannerMap.computeIfAbsent(banner.getAreaId(), areaId -> {

                AreaBannerCacheDto dto = new AreaBannerCacheDto();

                dto.setAreaId(areaId);

                return dto;
            });

            switch (banner.getBannerType()) {

                case "MAIN" -> areaBanner.getMainBanners().add(banner);

                case "BEST_RESTAURANT" -> areaBanner.getBestRestaurantBanners().add(banner);

                case "DEALS" -> areaBanner.getDealsBanners().add(banner);
            }
        }

        // Step 4 - Sort by Slot Number
        for (AreaBannerCacheDto area : areaBannerMap.values()) {

            area.getMainBanners().sort(Comparator.comparing(CustomerBannerDto::getSlotNumber));

            area.getBestRestaurantBanners().sort(Comparator.comparing(CustomerBannerDto::getSlotNumber));

            area.getDealsBanners().sort(Comparator.comparing(CustomerBannerDto::getSlotNumber));
        }
        // Step 5 - Clear Existing Redis Cache

        log.info("Clearing existing banner cache");

        Set<String> keys = redisTemplate.keys("AREA_*");

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        log.info("Existing banner cache cleared");

        // Step 6 - Store New Cache in Redis

        for (AreaBannerCacheDto area : areaBannerMap.values()) {

            String redisKey = "AREA_" + area.getAreaId();

            redisTemplate.opsForValue().set(redisKey, area);

            log.info("Banner cache stored | key={} | main={} | best={} | deals={}", redisKey, area.getMainBanners().size(), area.getBestRestaurantBanners().size(), area.getDealsBanners().size());
        }

        log.info("BANNER_CACHE_REFRESH_COMPLETED");
    }
}