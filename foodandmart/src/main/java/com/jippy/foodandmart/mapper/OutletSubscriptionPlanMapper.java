package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.OutletSubscriptionPlanResponseDto;
import com.jippy.foodandmart.entity.FmOutletSubscriptionPlan;

public final class OutletSubscriptionPlanMapper {

    private OutletSubscriptionPlanMapper() {
    }

    public static OutletSubscriptionPlanResponseDto toDto(
            FmOutletSubscriptionPlan entity) {

        OutletSubscriptionPlanResponseDto dto =
                new OutletSubscriptionPlanResponseDto();

        dto.setOutletSubscriptionPlanId(entity.getOutletSubscriptionPlanId());

        dto.setOutletId(entity.getOutletId());

        dto.setSubscriptionPlanId(entity.getSubscriptionPlanId());

        dto.setSubscriptionFromDate(entity.getSubscriptionFromDate());

        dto.setSubscriptionToDate(entity.getSubscriptionToDate());

        dto.setBannerSlotDaysId(entity.getBannerSlotDaysId());

        dto.setMealTypeTimingsIds(entity.getMealTypeTimingsIds());

        dto.setPriceModelType(entity.getPriceModelType());

        dto.setOfferAmount(entity.getOfferAmount());

        return dto;
    }
}