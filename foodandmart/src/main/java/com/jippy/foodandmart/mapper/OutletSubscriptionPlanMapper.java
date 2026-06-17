package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.OutletSubscriptionResponseDto;
import com.jippy.foodandmart.entity.FmSubscriptionPlan;
import com.jippy.foodandmart.entity.FmOutletSubscriptionPlan;
import org.springframework.stereotype.Component;

@Component
public class OutletSubscriptionPlanMapper {

    public OutletSubscriptionResponseDto toResponseDto(
            FmOutletSubscriptionPlan subscription,
            FmSubscriptionPlan plan) {

        OutletSubscriptionResponseDto dto =
                new OutletSubscriptionResponseDto();

        dto.setOutletSubscriptionPlanId(
                subscription.getOutletSubscriptionPlanId());

        dto.setOutletId(subscription.getOutletId());

        dto.setSubscriptionPlanId(plan.getSubscriptionPlanId());
        dto.setPlanName(plan.getPlanName());

        dto.setSubscriptionFromDate(
                subscription.getSubscriptionFromDate());

        dto.setSubscriptionToDate(
                subscription.getSubscriptionToDate());

        dto.setBannerSlots(plan.getBannerSlots());
        dto.setBannerDays(plan.getBannerDays());
        dto.setBestRestaurantSlot(plan.getBestRestaurantSlot());
        dto.setWhatsappBroadcast(plan.getWhatsappBroadcast());
        dto.setVideoCredits(plan.getVideoCredits());

        return dto;
    }
}