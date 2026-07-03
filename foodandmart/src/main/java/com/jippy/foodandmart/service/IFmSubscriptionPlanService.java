package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmApiResponse;
import com.jippy.foodandmart.dto.FmSubscriptionPlanRequestDto;
import com.jippy.foodandmart.dto.FmSubscriptionPlanResponseDto;
import com.jippy.foodandmart.dto.SubscriptionPlanResponseDto;

import java.util.List;

public interface IFmSubscriptionPlanService {

    SubscriptionPlanResponseDto saveOrUpdate(
            FmSubscriptionPlanRequestDto request);

    SubscriptionPlanResponseDto getById(Integer subscriptionPlanId);

    List<SubscriptionPlanResponseDto> getAll();

    void delete(Integer subscriptionPlanId);

    FmApiResponse<List<FmSubscriptionPlanResponseDto>> getSubscriptionPlansByAreaId(Integer areaId);

}