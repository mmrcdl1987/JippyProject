package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmSubscriptionPlanDto;
import com.jippy.foodandmart.dto.SubscriptionPlanResponseDto;

import java.util.List;

public interface IFmSubscriptionPlanService {
    FmSubscriptionPlanDto createAndUpdatePlans(FmSubscriptionPlanDto dto);
    List<SubscriptionPlanResponseDto> getSubscriptionPlans(Integer areaId);

}