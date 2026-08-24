package com.jippy.foodandmart.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.MealTypeTiming;
import com.jippy.foodandmart.mapper.CustomerBannerMapper;
import com.jippy.foodandmart.repository.MealTypeTimingRepository;
import com.jippy.foodandmart.service.BannerCacheService;
import com.jippy.foodandmart.service.OutletSubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;


import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerCacheServiceImpl implements BannerCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final OutletSubscriptionPlanService outletSubscriptionPlanService;
    private final ObjectMapper objectMapper;
    private final MealTypeTimingRepository mealTypeTimingRepository;

    private static final double MAX_SEARCH_RADIUS = 25.0; // Dynamic maximum threshold

    @Autowired
    private S3Client s3Client;

    @Value("${default-banners}")
    private String defaultBanners;

    private static final String GEO_KEY = "jippy:banner:geo";
    private static final String META_KEY = "jippy:banner:meta";


    // In-Memory cache for S3 banner URLs to avoid hitting S3 on every user request
    private final List<String> cachedDefaultS3Urls = new CopyOnWriteArrayList<>();

    // 1. MAIN LOCATION BANNER QUERY (Called by API Controller)
    @Override
    public GroupedBannerResponseDto getActiveBannersForLocation(double lat, double lng) {
        log.info("[FETCH START] Requesting active banners for Lat: {}, Lng: {}", lat, lng);

        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

        RedisGeoCommands.GeoSearchCommandArgs args = RedisGeoCommands.GeoSearchCommandArgs
                .newGeoSearchArgs()
                .includeDistance()
                .sortAscending();

        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = geoOps.search(
                GEO_KEY,
                GeoReference.fromCoordinate(lng, lat),
                new Distance(MAX_SEARCH_RADIUS, Metrics.KILOMETERS),
                args
        );

        int foundCount = (geoResults != null && geoResults.getContent() != null) ? geoResults.getContent().size() : 0;
        log.info("[GEO SEARCH] Initial search within {} KM found {} candidate outlets in Redis", MAX_SEARCH_RADIUS, foundCount);

        // Fallback refresh if Redis is completely empty
        if (geoResults == null || geoResults.getContent().isEmpty()) {
            log.warn("[REDIS EMPTY] Redis GEO Index empty. Triggering DB cache refresh...");
            refreshBannerCache();

            geoResults = geoOps.search(
                    GEO_KEY,
                    GeoReference.fromCoordinate(lng, lat),
                    new Distance(MAX_SEARCH_RADIUS, Metrics.KILOMETERS),
                    args
            );

            foundCount = (geoResults != null && geoResults.getContent() != null) ? geoResults.getContent().size() : 0;
            log.info("[FALLBACK SEARCH] Post-refresh GEO search found {} candidate outlets", foundCount);

            if (geoResults == null || geoResults.getContent().isEmpty()) {
                log.error("[NO MATCHES] No outlets found within radius even after cache refresh. Returning default response.");
                return buildGroupedBannerResponse(Collections.emptyList());
            }
        }

        List<String> nearbyOutletIds = geoResults.getContent().stream()
                .map(r -> r.getContent().getName())
                .collect(Collectors.toList());
        log.debug("[CANDIDATE OUTLETS] Outlet IDs near user: {}", nearbyOutletIds);

        List<String> rawMetadataList = hashOps.multiGet(META_KEY, nearbyOutletIds);
        List<CustomerBannerDto> activeOutlets = new ArrayList<>();

        for (int i = 0; i < geoResults.getContent().size(); i++) {
            var geoResult = geoResults.getContent().get(i);
            String outletId = geoResult.getContent().getName();
            double calculatedDistance = geoResult.getDistance().getValue();
            String jsonMeta = (rawMetadataList != null && i < rawMetadataList.size()) ? rawMetadataList.get(i) : null;

            if (jsonMeta == null) {
                log.warn("[MISSING META] No JSON metadata found in Redis HASH for outletId: {}", outletId);
                continue;
            }

            try {
                CustomerBannerDto outletDto = objectMapper.readValue(jsonMeta, CustomerBannerDto.class);

                // Distance Check against individual outlet's delivery radius
                double allowedRadius = outletDto.getRadiusInKms() != null ? outletDto.getRadiusInKms().doubleValue() : 3.0;
                if (calculatedDistance > allowedRadius) {
                    log.info("[SKIP DISTANCE] Outlet ID: {} distance ({} km) exceeds allowed radius ({} km)", outletId, calculatedDistance, allowedRadius);
                    continue;
                }

                // Meal Timing Check
                if (outletDto.getMealTypeTimingIds() != null && outletDto.getMealTypeTimingIds().length > 0) {
                    List<Integer> mealIds = Arrays.asList(outletDto.getMealTypeTimingIds());
                    boolean isMealActive = mealTypeTimingRepository.isMealActiveNow(mealIds, LocalTime.now());
                    if (!isMealActive) {
                        log.info("[SKIP MEAL TIMING] Outlet ID: {} meal timings {} are not active at {}", outletId, mealIds, LocalTime.now());
                        continue;
                    }
                }

                log.info("[OUTLET ACCEPTED] Outlet ID: {} passed radius and meal checks. Distance: {} km", outletId, calculatedDistance);
                activeOutlets.add(outletDto);
            } catch (Exception e) {
                log.error("[DESERIALIZE ERROR] Parsing error for outlet metadata, outletId: {}", outletId, e);
            }
        }

        log.info("[FETCH COMPLETE] Returning grouped banners built from {} active matching outlets", activeOutlets.size());
        return buildGroupedBannerResponse(activeOutlets);
    }

    // =========================================================================
// 2. CACHE REFRESH & TTL MANAGEMENT
// =========================================================================
    @Override
    public synchronized void refreshBannerCache() {
        log.info("[CACHE REFRESH START] Initiating Redis Banner Cache Refresh from DB...");

        // 1. Call existing method returning List<ActiveBannerResponseDto>
        List<ActiveBannerResponseDto> activeBanners = outletSubscriptionPlanService.getActiveBanners();

        if (activeBanners == null || activeBanners.isEmpty()) {
            log.warn("[DB EMPTY] No active banners returned from DB query.");
            return;
        }
        log.info("[DB FETCH SUCCESS] Retrieved {} banner records from DB", activeBanners.size());

        // 2. Clear old Redis Keys
        Boolean geoDeleted = redisTemplate.delete(GEO_KEY);
        Boolean metaDeleted = redisTemplate.delete(META_KEY);
        log.info("[REDIS CLEAR] Cleared old keys -> GEO Key ({}): {}, META Key ({}): {}", GEO_KEY, geoDeleted, META_KEY, metaDeleted);

        // 3. Group flat list of ActiveBannerResponseDto by Outlet ID
        Map<Integer, List<ActiveBannerResponseDto>> outletGroupedMap = activeBanners.stream()
                .filter(b -> b.getOutletId() != null)
                .collect(Collectors.groupingBy(ActiveBannerResponseDto::getOutletId));

        log.info("[GROUPING COMPLETE] Grouped {} DB records into {} distinct outlets", activeBanners.size(), outletGroupedMap.size());

        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

        int indexedCount = 0;

        // 4. Iterate over grouped outlets and save to Redis
        for (Map.Entry<Integer, List<ActiveBannerResponseDto>> entry : outletGroupedMap.entrySet()) {
            Integer outletId = entry.getKey();
            List<ActiveBannerResponseDto> outletBannerRows = entry.getValue();

            // Take first record for outlet metadata (Lat, Lng, Name, Radius)
            ActiveBannerResponseDto primaryRecord = outletBannerRows.get(0);
            System.out.println("===================primaryRecord========"+primaryRecord);

            if (primaryRecord.getLatitude() == null || primaryRecord.getLongitude() == null) {
                log.warn("[INDEX SKIP] Skipping outletId: {} due to missing Lat/Lng coordinates", outletId);
                continue;
            }

            // --- FILTER INACTIVE MEAL TIMINGS BEFORE WRITING TO REDIS ---
            if (primaryRecord.getMealTypeTimingsIds() != null && primaryRecord.getMealTypeTimingsIds().length > 0) {
                List<Integer> mealIds = Arrays.asList(primaryRecord.getMealTypeTimingsIds());
                boolean isMealActive = mealTypeTimingRepository.isMealActiveNow(mealIds, LocalTime.now());

                if (!isMealActive) {
                    log.info("[SKIP REDIS INDEX] Skipping outletId: {} - Meal timings {} are not active at {}", outletId, mealIds, LocalTime.now());
                    continue; // Do NOT add expired outlets to Redis!
                }
            }

            String outletIdStr = String.valueOf(outletId);

            // Pushing Location Data (Lng, Lat, OutletId) into Redis GEO Key
            geoOps.add(GEO_KEY, new Point(primaryRecord.getLongitude(), primaryRecord.getLatitude()), outletIdStr);

            // Mapping grouped rows into CustomerBannerDto and saving Metadata into Redis HASH Key
            CustomerBannerDto dto = CustomerBannerMapper.toDto(primaryRecord, outletBannerRows);
            System.out.println("===================dto========"+dto);
            try {
                String jsonStr = objectMapper.writeValueAsString(dto);
                hashOps.put(META_KEY, outletIdStr, jsonStr);
                indexedCount++;
                log.debug("[REDIS PUSH SUCCESS] Indexed Location & Metadata for outletId: {} (Lat: {}, Lng: {})",
                        outletIdStr, primaryRecord.getLatitude(), primaryRecord.getLongitude());
            } catch (Exception e) {
                log.error("[SERIALIZE ERROR] Failed to serialize CustomerBannerDto for outletId: {}", outletIdStr, e);
            }
        }

        log.info("[INDEX COMPLETE] Successfully indexed {} outlets into Redis GEO & HASH keys", indexedCount);

        // 5. Apply TTL based on meal timing
        long ttlSeconds = getSecondsUntilCurrentMealEnds();
        redisTemplate.expire(GEO_KEY, ttlSeconds, TimeUnit.SECONDS);
        redisTemplate.expire(META_KEY, ttlSeconds, TimeUnit.SECONDS);

        log.info("[TTL SET] Applied TTL of {} seconds ({} minutes) to GEO and META Redis keys", ttlSeconds, ttlSeconds / 60);

        // Refresh default S3 banners in memory
        refreshDefaultS3BannerUrls();
    }


    // =========================================================================
// 3. DTO BUILDERS & DEFAULT SLOT FILLERS
// =========================================================================
    private GroupedBannerResponseDto buildGroupedBannerResponse(List<CustomerBannerDto> activeOutlets) {
        List<String> defaultS3Urls = getDefaultBannerUrlsFromS3();
        log.debug("[BUILD RESPONSE] Building section slots using {} default S3 banner URLs", defaultS3Urls.size());

        GroupedBannerResponseDto groupedBannerResponseDto = new GroupedBannerResponseDto();
        groupedBannerResponseDto.setMainBannerInfoDtos(buildSectionSlots("MAIN", activeOutlets, defaultS3Urls));
        groupedBannerResponseDto.setBestRestaurantBannerInfoDtos(buildSectionSlots("BEST_RESTAURANT", activeOutlets, defaultS3Urls));
        groupedBannerResponseDto.setDealsBannerInfoDtos(buildSectionSlots("DEALS", activeOutlets, defaultS3Urls));

        return groupedBannerResponseDto;
    }

    private List<BannerInfoDto> buildSectionSlots(String sectionType, List<CustomerBannerDto> activeOutlets, List<String> defaultS3Urls) {
        Map<Integer, BannerInfoDto> slotMap = new HashMap<>();

        for (CustomerBannerDto outlet : activeOutlets) {
            if (outlet.getBannerInfoDtos() == null) {
                log.warn("[SLOT CHECK] Outlet ID {} has NULL bannerInfoDtos", outlet.getOutletId());
                continue;
            }

            for (BannerInfoDto info : outlet.getBannerInfoDtos()) {
                log.info("[SLOT CHECK] Outlet: {}, Section Needed: '{}', Banner Found: [Type='{}', Slot={}]",
                        outlet.getOutletId(), sectionType, info.getBannerType());
                if (sectionType.equalsIgnoreCase(info.getBannerType()) && info.getSlotNumber() != null) {
                    int slot = info.getSlotNumber();
                    BannerInfoDto bannerInfoDto = new BannerInfoDto();

                    bannerInfoDto.setBannerType(sectionType);
                    bannerInfoDto.setBannerUrl(info.getBannerUrl());
                    bannerInfoDto.setSlotNumber(slot);
                    bannerInfoDto.setOutletId(outlet.getOutletId());
                    bannerInfoDto.setOutletName(outlet.getOutletName());

                    slotMap.putIfAbsent(slot, bannerInfoDto);
                }
            }
        }

        List<BannerInfoDto> resultList = new ArrayList<>();
        for (int slot = 1; slot <= 6; slot++) {
            if (slotMap.containsKey(slot)) {
                resultList.add(slotMap.get(slot));
            } else {
                String fallbackUrl = (defaultS3Urls != null && !defaultS3Urls.isEmpty())
                        ? defaultS3Urls.get((slot - 1) % defaultS3Urls.size())
                        : "https://jippys3bucket.s3.ap-south-2.amazonaws.com/jippy-banners/default-placeholder.jpg";

                BannerInfoDto bannerInfoDto = new BannerInfoDto();
                bannerInfoDto.setBannerType("DEFAULT");
                bannerInfoDto.setBannerUrl(fallbackUrl);
                bannerInfoDto.setSlotNumber(slot);

                resultList.add(bannerInfoDto);
            }
        }

        resultList.sort(Comparator.comparing(BannerInfoDto::getSlotNumber));
        log.debug("[SECTION BUILT] Section: '{}' mapped with {} occupied active slots and defaults", sectionType, slotMap.size());
        return resultList;
    }

    // =========================================================================
// 4. HELPER METHODS (TTL & S3 FETCHING)
// =========================================================================
    private long getSecondsUntilCurrentMealEnds() {
        LocalTime now = LocalTime.now();
        List<MealTypeTiming> timings = mealTypeTimingRepository.findAll();
        log.debug("[TTL CALC] Checking current time {} against {} meal timing windows", now, timings.size());

        for (MealTypeTiming timing : timings) {
            LocalTime from = timing.getFromTime();
            LocalTime to = timing.getToTime();

            if (from == null || to == null) continue;

            boolean isActive;
            if (to.isAfter(from)) {
                isActive = !now.isBefore(from) && now.isBefore(to);
            } else {
                isActive = !now.isBefore(from) || now.isBefore(to);
            }

            if (isActive) {
                long secondsRemaining;
                if (to.isAfter(from)) {
                    secondsRemaining = Duration.between(now, to).getSeconds();
                } else {
                    if (now.isAfter(from)) {
                        secondsRemaining = Duration.between(now, LocalTime.MAX).getSeconds()
                                + Duration.between(LocalTime.MIN, to).getSeconds() + 1;
                    } else {
                        secondsRemaining = Duration.between(now, to).getSeconds();
                    }
                }
                long finalTtl = Math.max(secondsRemaining, 60);
                log.info("[TTL CALC MATCH] Active meal slot found from {} to {}. TTL calculated: {} seconds", from, to, finalTtl);
                return finalTtl;
            }
        }

        log.warn("[TTL CALC FALLBACK] No active meal slot matched at {}. Using default TTL: 600 seconds", now);
        return 600L;
    }

    public List<String> getDefaultBannerUrlsFromS3() {
        if (cachedDefaultS3Urls.isEmpty()) {
            log.warn("[S3 CACHE EMPTY] In-memory default S3 URLs empty. Fetching from AWS S3...");
            refreshDefaultS3BannerUrls();
        }
        return cachedDefaultS3Urls;
    }

    private void refreshDefaultS3BannerUrls() {
        List<String> defaultUrls = new ArrayList<>();

        try {
            URI uri = new URI(defaultBanners);
            String host = uri.getHost();
            String bucketName = host.split("\\.")[0];

            String prefix = uri.getPath();
            if (prefix.startsWith("/")) {
                prefix = prefix.substring(1);
            }

            log.info("[S3 FETCH] Requesting objects from S3 Bucket: '{}', Prefix: '{}'", bucketName, prefix);

            ListObjectsV2Request req = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response result = s3Client.listObjectsV2(req);

            for (S3Object s3Object : result.contents()) {
                String key = s3Object.key();

                if (!key.equals(prefix) &&
                        (key.endsWith(".png") || key.endsWith(".jpg") || key.endsWith(".jpeg") || key.endsWith(".webp"))) {

                    String fileUrl = defaultBanners.endsWith("/")
                            ? defaultBanners + key.substring(prefix.length())
                            : defaultBanners + "/" + key.substring(prefix.length());

                    defaultUrls.add(fileUrl);
                }
            }

            if (!defaultUrls.isEmpty()) {
                cachedDefaultS3Urls.clear();
                cachedDefaultS3Urls.addAll(defaultUrls);
                log.info("[S3 FETCH SUCCESS] Retrieved and cached {} default banner URLs from S3", defaultUrls.size());
            } else {
                log.warn("[S3 FETCH EMPTY] No image files matching extensions found in S3 path: {}", defaultBanners);
            }
        } catch (Exception e) {
            log.error("[S3 FETCH ERROR] Failed to fetch default banner images from S3 URL: {}", defaultBanners, e);
        }
    }
}