package com.jippy.foodandmart.dto;

import com.jippy.foodandmart.enums.PromotionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class PromotionPlanAuditResponseDto {

    private Integer promotionPlanId;

    private Integer outletId;

    private Integer promotionPlanTypeId;

    private String promotionPlanType;

    private LocalDate planStartDate;

    private LocalDate planEndDate;

    private LocalTime planStartTime;

    private LocalTime planEndTime;

    private String offerName;

    private BigDecimal minimumOrderValue;

    private BigDecimal offerAmount;

    private String offerType;

    /**
     * ACTIVE / SCHEDULED / ENDED
     */
    private PromotionStatus status;

    private List<Integer> productIds;

    private List<Integer> outletCategoryIds;

    private Integer createdBy;

    private LocalDateTime createdAt;

    private Integer updatedBy;

    private LocalDateTime updatedAt;
}