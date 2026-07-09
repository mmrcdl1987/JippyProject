package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.BannerSlotDayResponseDto;
import com.jippy.foodandmart.service.BannerSlotDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fm/banner-slots")
@RequiredArgsConstructor
public class BannerSlotDayController {

    private final BannerSlotDayService bannerSlotDayService;

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

}