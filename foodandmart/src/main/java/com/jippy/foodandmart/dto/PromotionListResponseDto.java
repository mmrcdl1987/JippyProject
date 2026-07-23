package com.jippy.foodandmart.dto;

import com.jippy.foodandmart.enums.PromotionStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class PromotionListResponseDto {

    private Integer promotionPlanId;

    private String offerName;

    private String offerType;

    private BigDecimal offerAmount;

    private LocalDate planStartDate;

    private LocalDate planEndDate;

    private LocalTime planStartTime;

    private LocalTime planEndTime;

    private PromotionStatus status;
}