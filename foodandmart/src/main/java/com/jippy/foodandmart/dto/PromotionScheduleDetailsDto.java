package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class PromotionScheduleDetailsDto {

    private Integer promotionPlanId;

    private Integer outletId;

    private Integer areaId;

    private LocalDate planStartDate;

    private LocalDate planEndDate;

    private LocalTime planStartTime;

    private LocalTime planEndTime;

    private List<Integer> productIds;
}