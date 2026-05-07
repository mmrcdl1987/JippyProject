package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmSubscriptionPlanDto;

public interface IFmSubscriptionPlanService
{
    FmSubscriptionPlanDto createAndUpdatePlans(FmSubscriptionPlanDto dto);
}