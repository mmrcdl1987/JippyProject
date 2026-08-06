package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.enums.PromotionStatus;
import com.jippy.foodandmart.service.IPromotionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Promotion Plan API", description = "REST APIs for managing merchant promotion plans")
public class PromotionPlanController {

    private final IPromotionPlanService promotionPlanService;

    /**
     * Create Promotion Plan
     */
    @Operation(summary = "Create Promotion Plan")
    @PostMapping
    public ResponseEntity<PromotionPlanAuditResponseDto> createPromotionPlan(@Valid @RequestBody PromotionPlanRequestDto requestDto) {

        log.info("[PROMOTION-PLAN] Create Promotion Plan API Started");

        PromotionPlanAuditResponseDto response = promotionPlanService.createPromotionPlan(requestDto);

        log.info("[PROMOTION-PLAN] Create Promotion Plan API Completed");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get Promotion Plan By Id
     */
    @Operation(summary = "Get Promotion Plan By Id")
    @GetMapping("/{promotionPlanId}")
    public ResponseEntity<PromotionPlanResponseDto> getPromotionPlanById(@PathVariable Integer promotionPlanId) {

        log.info("[PROMOTION-PLAN] Get Promotion Plan By Id API Started. promotionPlanId={}", promotionPlanId);

        PromotionPlanResponseDto response = promotionPlanService.getPromotionPlanById(promotionPlanId);

        log.info("[PROMOTION-PLAN] Get Promotion Plan By Id API Completed");

        return ResponseEntity.ok(response);
    }

    /**
     * Get All Promotion Plans
     */
    @Operation(summary = "Get All Promotion Plans")
    @GetMapping
    public ResponseEntity<List<PromotionPlanResponseDto>> getAllPromotionPlans() {

        log.info("[PROMOTION-PLAN] Get All Promotion Plans API Started");

        List<PromotionPlanResponseDto> response = promotionPlanService.getAllPromotionPlans();

        log.info("[PROMOTION-PLAN] Get All Promotion Plans API Completed");

        return ResponseEntity.ok(response);
    }

    /**
     * Update Promotion Plan
     */
    @Operation(summary = "Update Promotion Plan")
    @PutMapping("/{promotionPlanId}")
    public ResponseEntity<PromotionPlanAuditResponseDto> updatePromotionPlan(@PathVariable Integer promotionPlanId, @Valid @RequestBody PromotionPlanRequestDto requestDto) {

        log.info("[PROMOTION-PLAN] Update Promotion Plan API Started. promotionPlanId={}", promotionPlanId);

        PromotionPlanAuditResponseDto response = promotionPlanService.updatePromotionPlan(promotionPlanId, requestDto);

        log.info("[PROMOTION-PLAN] Update Promotion Plan API Completed");

        return ResponseEntity.ok(response);
    }

    /**
     * Delete Promotion Plan
     */
    @Operation(summary = "Delete Promotion Plan")
    @DeleteMapping("/{promotionPlanId}")
    public ResponseEntity<FmApiResponse<Void>> deletePromotionPlan(@PathVariable Integer promotionPlanId) {

        log.info("[PROMOTION-PLAN] Delete Promotion Plan API Started. promotionPlanId={}", promotionPlanId);

        FmApiResponse<Void> response = promotionPlanService.deletePromotionPlan(promotionPlanId);

        log.info("[PROMOTION-PLAN] Delete Promotion Plan API Completed");

        return ResponseEntity.ok(response);
    }

    /**
     * Get Promotion Plans By Outlet
     */
    @Operation(summary = "Get Promotion Plans By Outlet")
    @GetMapping("/outlets/{outletId}")
    public ResponseEntity<FmApiResponse<PageResponseDto<PromotionListResponseDto>>> getPromotionPlans(

            @PathVariable Integer outletId,

            @RequestParam(defaultValue = "ALL") PromotionStatus status,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size,

            @RequestParam(defaultValue = "promotionPlanId") String sortBy,

            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("[PROMOTION-PLAN] Get Promotion Plans API Started. outletId={}, status={}, page={}, size={}", outletId, status, page, size);

        FmApiResponse<PageResponseDto<PromotionListResponseDto>> response = promotionPlanService.getPromotionPlans(outletId, status, page, size, sortBy, direction);

        log.info("[PROMOTION-PLAN] Get Promotion Plans API Completed");

        return ResponseEntity.ok(response);
    }

    /**
     * Get Promotion Status Counts
     */
    @Operation(summary = "Get Promotion Status Counts")
    @GetMapping("/outlets/{outletId}/counts")
    public ResponseEntity<FmApiResponse<PromotionStatusCountDto>> getPromotionStatusCounts(@PathVariable Integer outletId) {

        log.info("[PROMOTION-PLAN] Get Promotion Status Counts API Started. outletId={}", outletId);

        FmApiResponse<PromotionStatusCountDto> response = FmApiResponse.success("Promotion status counts fetched successfully.", promotionPlanService.getPromotionStatusCounts(outletId));

        log.info("[PROMOTION-PLAN] Get Promotion Status Counts API Completed");

        return ResponseEntity.ok(response);
    }

    /**
     * Get Promotion Schedule Details
     * Used by Division Service to generate Promotion Schedules.
     */
    @Operation(summary = "Get Promotion Schedule Details")
    @GetMapping("/{promotionPlanId}/schedule-details")
    public ResponseEntity<PromotionScheduleDetailsDto> getPromotionScheduleDetails(@PathVariable Integer promotionPlanId) {

        log.info("[PROMOTION-PLAN] Get Promotion Schedule Details API Started. promotionPlanId={}", promotionPlanId);

        PromotionScheduleDetailsDto response = promotionPlanService.getPromotionScheduleDetails(promotionPlanId);

        log.info("[PROMOTION-PLAN] Get Promotion Schedule Details API Completed");

        return ResponseEntity.ok(response);
    }
}