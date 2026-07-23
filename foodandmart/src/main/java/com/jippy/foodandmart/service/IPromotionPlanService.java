package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.PageResponseDto;
import com.jippy.foodandmart.dto.PromotionListResponseDto;
import com.jippy.foodandmart.dto.PromotionPlanAuditResponseDto;
import com.jippy.foodandmart.dto.PromotionPlanRequestDto;
import com.jippy.foodandmart.dto.PromotionPlanResponseDto;
import com.jippy.foodandmart.dto.PromotionStatusCountDto;
import com.jippy.foodandmart.enums.PromotionStatus;

import java.util.List;

public interface IPromotionPlanService {

    /**
     * Create Promotion Plan with Products/Categories
     */
    PromotionPlanAuditResponseDto createPromotionPlan(
            PromotionPlanRequestDto requestDto);

    /**
     * Get Promotion Plan By Id
     */
    PromotionPlanResponseDto getPromotionPlanById(
            Integer promotionPlanId);

    /**
     * Get All Promotion Plans
     */
    List<PromotionPlanResponseDto> getAllPromotionPlans();

    /**
     * Update Promotion Plan with Products/Categories
     */
    PromotionPlanAuditResponseDto updatePromotionPlan(
            Integer promotionPlanId,
            PromotionPlanRequestDto requestDto);

    /**
     * Delete Promotion Plan
     */
    FmApiResponse<Void> deletePromotionPlan(
            Integer promotionPlanId);

    /**
     * Dashboard Listing
     */
    FmApiResponse<PageResponseDto<PromotionListResponseDto>> getPromotionPlans(
            Integer outletId,
            PromotionStatus status,
            int page,
            int size,
            String sortBy,
            String direction);

    /**
     * Promotion Status Counts
     */
    PromotionStatusCountDto getPromotionStatusCounts(
            Integer outletId);
}