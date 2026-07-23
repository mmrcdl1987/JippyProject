package com.jippy.foodandmart.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromotionPlanTypeAuditResponseDto {

    private Integer promotionPlanTypesId;

    private String planName;

    private Integer createdBy;

    private LocalDateTime createdAt;

    private Integer updatedBy;

    private LocalDateTime updatedAt;
}