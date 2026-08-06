package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.PromotionScheduleDetailsDto;
import com.jippy.foodandmart.service.IPromotionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fm/internal/promotion-plans")
@RequiredArgsConstructor
@Slf4j
public class InternalPromotionController {

    private final IPromotionPlanService promotionPlanService;

    @GetMapping("/{promotionPlanId}/schedule-details")
    public ResponseEntity<PromotionScheduleDetailsDto> getPromotionScheduleDetails(
            @PathVariable Integer promotionPlanId) {

        log.info(
                "[INTERNAL-PROMOTION] Fetch schedule details | promotionPlanId={}",
                promotionPlanId);

        return ResponseEntity.ok(
                promotionPlanService.getPromotionScheduleDetails(
                        promotionPlanId));
    }
}