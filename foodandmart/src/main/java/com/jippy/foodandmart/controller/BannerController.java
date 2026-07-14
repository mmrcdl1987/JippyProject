package com.jippy.foodandmart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.foodandmart.dto.BannerSlotDayResponseDto;
import com.jippy.foodandmart.dto.CustomerBannerDto;
import com.jippy.foodandmart.service.BannerSlotDayService;
import com.jippy.foodandmart.service.MealTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fm/banners")
@RequiredArgsConstructor
@Slf4j
public class BannerController {

    private final BannerSlotDayService bannerSlotDayService;
    private final RedisTemplate<String, String> redisTemplate;
    private final MealTimeService mealTimeService;

    private final ObjectMapper objectMapper;

    private static final String GEO_KEY = "jippy:banner:geo";
    private static final String META_KEY = "jippy:banner:meta";
    private static final double MAX_SEARCH_RADIUS = 25.0; // Dynamic maximum threshold

    @PostMapping("/generate")
    public ResponseEntity<String> generateInitialSlots() {

        bannerSlotDayService.generateInitialFourMonths();

        return ResponseEntity.ok("Banner slots generated successfully.");
    }

    @PostMapping("/maintain")
    public ResponseEntity<String> maintainSlots() {

        bannerSlotDayService.maintainBannerSlots();

        return ResponseEntity.ok("Banner slots maintained successfully.");
    }

    @GetMapping
    public ResponseEntity<List<BannerSlotDayResponseDto>> getAllSlots() {

        return ResponseEntity.ok(
                bannerSlotDayService.getAllSlots());
    }

    @GetMapping("/getActiveBanners")
    public ResponseEntity<List<CustomerBannerDto>> getActiveBanners(
            @RequestParam double lat,
            @RequestParam double lng) {

        log.info("getActiveBanners API called for given latitude : {}, longitude:{}", lat, lng);
        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

        // 1. Define the search area (circle around customer coordinates)
        Circle area = new Circle(new Point(lng, lat), new Distance(MAX_SEARCH_RADIUS, Metrics.KILOMETERS));

        // 2. Build the GeoSearchSpec combining the key, the circular area, and your arguments
        RedisGeoCommands.GeoSearchCommandArgs args = RedisGeoCommands.GeoSearchCommandArgs
                .newGeoSearchArgs()
                .includeDistance()
                .sortAscending();

        // 3. Execute search with the 2-parameter signature: search(key, GeoReference, GeoSearchCommandArgs)
        // Note: We wrap our circle as a GeoReference
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = geoOps.search(
                GEO_KEY,
                GeoReference.fromCoordinate(lng, lat),
                new Distance(MAX_SEARCH_RADIUS, Metrics.KILOMETERS),
                args
        );

        if (geoResults == null || geoResults.getContent().isEmpty()) {
            log.warn(">>> Step 1: Redis GEO search returned 0 results nearby!");
            return ResponseEntity.ok(Collections.emptyList());
        }
        log.info(">>> Step 1: Redis GEO found {} outlets nearby.", geoResults.getContent().size());

        // Extract found IDs
        List<String> nearbyIds = geoResults.getContent().stream()
                .map(r -> r.getContent().getName())
                .collect(Collectors.toList());

        // 2. Multi-get all metadata JSON strings in a single Redis query
        List<String> rawMetadataList = hashOps.multiGet(META_KEY, nearbyIds);
        log.info("==========================rawMetadataList" + rawMetadataList.toString());
        List<CustomerBannerDto> matchingBanners = new ArrayList<>();

        for (int i = 0; i < geoResults.getContent().size(); i++) {
            var geoResult = geoResults.getContent().get(i);
            double calculatedDistance = geoResult.getDistance().getValue(); // Actual distance in KM
            String jsonMeta = rawMetadataList.get(i);

            if (jsonMeta == null) {
                log.warn(">>> Outlet ID {} has coordinates in GEO but is missing metadata in HASH!", geoResult.getContent().getName());
                continue;
            }

            try {
                CustomerBannerDto banner = objectMapper.readValue(jsonMeta, CustomerBannerDto.class);
                log.info("Checking outlet: {} (Distance: {} km)", banner.getOutletName(), calculatedDistance);
                // 3. Apply memory filters
                // Filter A: Distance constraint
                double allowedRadius = banner.getRadiusInKms() != null ? banner.getRadiusInKms().doubleValue() : 3.0;
                if (calculatedDistance > allowedRadius) {
                    log.info("   -> SKIPPED: Distance ({} km) exceeds allowed radius ({} km)", calculatedDistance, allowedRadius);
                    continue;
                }

                // Filter B: Meal Timing Check
                List<Integer> bannerMealIds = Arrays.stream(banner.getMealTypeTimingIds()).toList();
                boolean isMealActive = mealTimeService.isMealActiveNow(bannerMealIds);
                log.info("   -> Outlet meal IDs: {}. Is active now? {}", bannerMealIds, isMealActive);

                if (!isMealActive) {
                    log.info("   -> SKIPPED: No matching active meal slot for the current time.");
                    continue;
                }

                matchingBanners.add(banner);
            } catch (Exception e) {
                // Log mapping parsing exception
            }
        }


            // Optional: Perform final sorting (e.g., by tier slot)
            // matchingBanners.sort(Comparator.comparing(ActiveBannerCache::getBannerSlot).reversed());

        return ResponseEntity.ok(matchingBanners);
    }

}