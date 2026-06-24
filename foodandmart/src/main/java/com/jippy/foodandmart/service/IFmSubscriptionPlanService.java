package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmSubscriptionPlanRequestDto;
import com.jippy.foodandmart.dto.SubscriptionPlanResponseDto;

import java.util.List;

public interface IFmSubscriptionPlanService {
    SubscriptionPlanResponseDto saveOrUpdate(
            FmSubscriptionPlanRequestDto request);

    SubscriptionPlanResponseDto getById(Integer subscriptionPlanId);

    List<SubscriptionPlanResponseDto> getAll();

    void delete(Integer subscriptionPlanId);
}