package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.PromotionPlanTypeAuditResponseDto;
import com.jippy.foodandmart.dto.PromotionPlanTypeRequestDto;
import com.jippy.foodandmart.dto.PromotionPlanTypeResponseDto;
import com.jippy.foodandmart.entity.PromotionPlanType;
import org.springframework.stereotype.Component;

@Component
public class PromotionPlanTypeMapper {

    /**
     * Request DTO -> Entity
     */
    public PromotionPlanType toEntity(PromotionPlanTypeRequestDto dto) {

        PromotionPlanType entity = new PromotionPlanType();

        entity.setPlanName(dto.getPlanName());

        return entity;
    }

    /**
     * Entity -> GET Response DTO
     */
    public PromotionPlanTypeResponseDto toResponseDto(
            PromotionPlanType entity) {

        PromotionPlanTypeResponseDto dto =
                new PromotionPlanTypeResponseDto();

        dto.setPromotionPlanTypesId(entity.getPromotionPlanTypesId());
        dto.setPlanName(entity.getPlanName());

        return dto;
    }

    /**
     * Entity -> POST/PUT Response DTO
     */
    public PromotionPlanTypeAuditResponseDto toAuditResponseDto(
            PromotionPlanType entity) {

        PromotionPlanTypeAuditResponseDto dto =
                new PromotionPlanTypeAuditResponseDto();

        dto.setPromotionPlanTypesId(entity.getPromotionPlanTypesId());
        dto.setPlanName(entity.getPlanName());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());

        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    /**
     * Update existing entity
     */
    public void updateEntity(PromotionPlanType entity,
                             PromotionPlanTypeRequestDto dto) {

        entity.setPlanName(dto.getPlanName());
    }
}