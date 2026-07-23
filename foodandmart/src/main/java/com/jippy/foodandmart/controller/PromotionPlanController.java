package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.enums.PromotionStatus;
import com.jippy.foodandmart.service.IPromotionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fm/promotion-plans")
@RequiredArgsConstructor
@Slf4j
public class  PromotionPlanController {

    private final IPromotionPlanService promotionPlanService;

    /**
     * Create Promotion Plan
     */
    @PostMapping
    public ResponseEntity<PromotionPlanAuditResponseDto> createPromotionPlan(
            @Valid @RequestBody PromotionPlanRequestDto requestDto) {

        log.info("[PROMOTION-PLAN] POST /api/fm/promotion-plans");

        PromotionPlanAuditResponseDto response =
                promotionPlanService.createPromotionPlan(requestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get Promotion Plan By Id
     */
    @GetMapping("/{promotionPlanId}")
    public ResponseEntity<PromotionPlanResponseDto> getPromotionPlanById(
            @PathVariable Integer promotionPlanId) {

        log.info("[PROMOTION-PLAN] GET /api/fm/promotion-plans/{}",
                promotionPlanId);

        PromotionPlanResponseDto response =
                promotionPlanService.getPromotionPlanById(
                        promotionPlanId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get All Promotion Plans
     */
    @GetMapping
    public ResponseEntity<List<PromotionPlanResponseDto>> getAllPromotionPlans() {

        log.info("[PROMOTION-PLAN] GET /api/fm/promotion-plans");

        List<PromotionPlanResponseDto> response =
                promotionPlanService.getAllPromotionPlans();

        return ResponseEntity.ok(response);
    }

    /**
     * Update Promotion Plan
     */
    @PutMapping("/{promotionPlanId}")
    public ResponseEntity<PromotionPlanAuditResponseDto> updatePromotionPlan(
            @PathVariable Integer promotionPlanId,
            @Valid @RequestBody PromotionPlanRequestDto requestDto) {

        log.info("[PROMOTION-PLAN] PUT /api/fm/promotion-plans/{}",
                promotionPlanId);

        PromotionPlanAuditResponseDto response =
                promotionPlanService.updatePromotionPlan(
                        promotionPlanId,
                        requestDto);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete Promotion Plan
     */
    @DeleteMapping("/{promotionPlanId}")
    public ResponseEntity<FmApiResponse<Void>> deletePromotionPlan(
            @PathVariable Integer promotionPlanId) {

        log.info(
                "[PROMOTION-PLAN] DELETE /api/fm/promotion-plans/{}",
                promotionPlanId);

        return ResponseEntity.ok(
                promotionPlanService.deletePromotionPlan(
                        promotionPlanId));
    }
    /**
     * Get Promotion Plans By Outlet
     */
    @GetMapping("/outlets/{outletId}")
    public ResponseEntity<FmApiResponse<PageResponseDto<PromotionListResponseDto>>> getPromotionPlans(

            @PathVariable Integer outletId,

            @RequestParam(defaultValue = "ALL")
            PromotionStatus status,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "promotionPlanId")
            String sortBy,

            @RequestParam(defaultValue = "DESC")
            String direction) {

        log.info(
                "[PROMOTION-PLAN] GET /api/fm/promotion-plans/outlets/{} | status={} | page={} | size={}",
                outletId,
                status,
                page,
                size);

        return ResponseEntity.ok(
                promotionPlanService.getPromotionPlans(
                        outletId,
                        status,
                        page,
                        size,
                        sortBy,
                        direction));
    }
    /**
     * Get Promotion Status Counts
     */
    @GetMapping("/outlets/{outletId}/counts")
    public ResponseEntity<FmApiResponse<PromotionStatusCountDto>> getPromotionStatusCounts(
            @PathVariable Integer outletId) {

        log.info(
                "[PROMOTION-PLAN] GET /api/fm/promotion-plans/outlets/{}/counts",
                outletId);

        return ResponseEntity.ok(
                FmApiResponse.success(
                        "Promotion status counts fetched successfully.",
                        promotionPlanService.getPromotionStatusCounts(outletId)));
    }
}