package com.jippy.foodandmart.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.foodandmart.dto.BannerSlotDayResponseDto;
import com.jippy.foodandmart.dto.CustomerBannerDto;
import com.jippy.foodandmart.dto.GroupedBannerResponseDto;
import com.jippy.foodandmart.service.BannerCacheService;
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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fm/banners")
@RequiredArgsConstructor
@Slf4j
public class BannerController {

    private final BannerSlotDayService bannerSlotDayService;
    private final RedisTemplate<String, String> redisTemplate;
    private final MealTimeService mealTimeService;
    private final BannerCacheService bannerCacheService;
    private final ObjectMapper objectMapper;

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
    public ResponseEntity<GroupedBannerResponseDto> getActiveBanners(
            @RequestParam double lat,
            @RequestParam double lng) {

        GroupedBannerResponseDto response = bannerCacheService.getActiveBannersForLocation(lat, lng);
        return ResponseEntity.ok(response);
    }
}