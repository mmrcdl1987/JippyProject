package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.PromotionPlanTypeAuditResponseDto;
import com.jippy.foodandmart.dto.PromotionPlanTypeRequestDto;
import com.jippy.foodandmart.dto.PromotionPlanTypeResponseDto;

import java.util.List;

public interface IPromotionPlanTypeService {

    PromotionPlanTypeAuditResponseDto createPromotionPlanType(
            PromotionPlanTypeRequestDto requestDto);

    PromotionPlanTypeResponseDto getPromotionPlanTypeById(
            Integer promotionPlanTypeId);

    List<PromotionPlanTypeResponseDto> getAllPromotionPlanTypes();

    PromotionPlanTypeAuditResponseDto updatePromotionPlanType(
            Integer promotionPlanTypeId,
            PromotionPlanTypeRequestDto requestDto);

    void deletePromotionPlanType(Integer promotionPlanTypeId);
}