package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.PromotionListResponseDto;
import com.jippy.foodandmart.dto.PromotionPlanAuditResponseDto;
import com.jippy.foodandmart.dto.PromotionPlanRequestDto;
import com.jippy.foodandmart.dto.PromotionPlanResponseDto;
import com.jippy.foodandmart.entity.PromotionPlan;
import com.jippy.foodandmart.enums.PromotionStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PromotionPlanMapper {

    public PromotionPlan toEntity(PromotionPlanRequestDto dto) {

        PromotionPlan entity = new PromotionPlan();

        entity.setPlanStartDate(dto.getPlanStartDate());
        entity.setPlanEndDate(dto.getPlanEndDate());
        entity.setPlanStartTime(dto.getPlanStartTime());
        entity.setPlanEndTime(dto.getPlanEndTime());

        entity.setOfferName(dto.getOfferName());
        entity.setMinimumOrderValue(dto.getMinimumOrderValue());
        entity.setOfferAmount(dto.getOfferAmount());
        entity.setOfferType(dto.getOfferType());

        return entity;
    }

    public PromotionPlanResponseDto toResponseDto(PromotionPlan entity) {

        PromotionPlanResponseDto dto = new PromotionPlanResponseDto();

        dto.setPromotionPlanId(entity.getPromotionPlanId());
        dto.setOutletId(entity.getOutletId());

        dto.setPromotionPlanTypeId(
                entity.getPromotionPlanType().getPromotionPlanTypesId());

        dto.setPromotionPlanType(
                entity.getPromotionPlanType().getPlanName());

        dto.setPlanStartDate(entity.getPlanStartDate());
        dto.setPlanEndDate(entity.getPlanEndDate());

        dto.setPlanStartTime(entity.getPlanStartTime());
        dto.setPlanEndTime(entity.getPlanEndTime());

        dto.setOfferName(entity.getOfferName());
        dto.setMinimumOrderValue(entity.getMinimumOrderValue());
        dto.setOfferAmount(entity.getOfferAmount());
        dto.setOfferType(entity.getOfferType());

        // If PromotionPlanResponseDto has status field
        dto.setStatus(getPromotionStatus(entity));

        return dto;
    }

    public PromotionPlanAuditResponseDto toAuditResponseDto(PromotionPlan entity) {

        PromotionPlanAuditResponseDto dto = new PromotionPlanAuditResponseDto();

        dto.setPromotionPlanId(entity.getPromotionPlanId());
        dto.setOutletId(entity.getOutletId());

        dto.setPromotionPlanTypeId(
                entity.getPromotionPlanType().getPromotionPlanTypesId());

        dto.setPromotionPlanType(
                entity.getPromotionPlanType().getPlanName());

        dto.setPlanStartDate(entity.getPlanStartDate());
        dto.setPlanEndDate(entity.getPlanEndDate());

        dto.setPlanStartTime(entity.getPlanStartTime());
        dto.setPlanEndTime(entity.getPlanEndTime());

        dto.setOfferName(entity.getOfferName());
        dto.setMinimumOrderValue(entity.getMinimumOrderValue());
        dto.setOfferAmount(entity.getOfferAmount());
        dto.setOfferType(entity.getOfferType());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());

        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // If PromotionPlanAuditResponseDto has status field
        dto.setStatus(getPromotionStatus(entity));

        return dto;
    }

    public void updateEntity(PromotionPlan entity,
                             PromotionPlanRequestDto dto) {

        entity.setPlanStartDate(dto.getPlanStartDate());
        entity.setPlanEndDate(dto.getPlanEndDate());

        entity.setPlanStartTime(dto.getPlanStartTime());
        entity.setPlanEndTime(dto.getPlanEndTime());

        entity.setOfferName(dto.getOfferName());
        entity.setMinimumOrderValue(dto.getMinimumOrderValue());
        entity.setOfferAmount(dto.getOfferAmount());
        entity.setOfferType(dto.getOfferType());
    }

    public PromotionListResponseDto toPromotionListResponseDto(PromotionPlan entity) {

        PromotionListResponseDto dto = new PromotionListResponseDto();

        dto.setPromotionPlanId(entity.getPromotionPlanId());
        dto.setOfferName(entity.getOfferName());

        // Keep enum if DTO field is OfferType.
        // If DTO field is String, use .name()
        dto.setOfferType(entity.getOfferType());

        dto.setOfferAmount(entity.getOfferAmount());

        dto.setPlanStartDate(entity.getPlanStartDate());
        dto.setPlanEndDate(entity.getPlanEndDate());

        dto.setPlanStartTime(entity.getPlanStartTime());
        dto.setPlanEndTime(entity.getPlanEndTime());

        dto.setStatus(getPromotionStatus(entity));

        return dto;
    }

    private PromotionStatus getPromotionStatus(PromotionPlan entity) {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startDateTime = LocalDateTime.of(
                entity.getPlanStartDate(),
                entity.getPlanStartTime());

        LocalDateTime endDateTime = LocalDateTime.of(
                entity.getPlanEndDate(),
                entity.getPlanEndTime());

        if (now.isBefore(startDateTime)) {
            return PromotionStatus.SCHEDULED;
        }

        if (now.isAfter(endDateTime)) {
            return PromotionStatus.ENDED;
        }

        return PromotionStatus.ACTIVE;
    }
}