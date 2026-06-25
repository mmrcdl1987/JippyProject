package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmSubscriptionPlanRequestDto;
import com.jippy.foodandmart.dto.SubscriptionPlanResponseDto;
import com.jippy.foodandmart.entity.FmSubscriptionPlan;

import java.time.LocalDateTime;

public final class SubscriptionPlanMapper {

    private SubscriptionPlanMapper() {
    }

    public static FmSubscriptionPlan toEntity(
            FmSubscriptionPlanRequestDto request) {

        FmSubscriptionPlan entity = new FmSubscriptionPlan();

        entity.setPlanName(request.getPlanName());
        entity.setPrice(request.getPrice());
        entity.setDurationInDays(request.getDurationInDays());
        entity.setBannerDurationInDays(request.getBannerDurationInDays());
        entity.setRadiusInKms(request.getRadiusInKms());
        entity.setBannerSlot(request.getBannerSlot());
        entity.setBestRestaurantSlot(request.getBestRestaurantSlot());
        entity.setDealsSlot(request.getDealsSlot());
        entity.setWhatsappBroadcast(request.getWhatsappBroadcast());
        entity.setVideoCredits(request.getVideoCredits());
        entity.setStateId(request.getStateId());
        entity.setCityId(request.getCityId());
        entity.setAreaId(request.getAreaId());

        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(request.getUserId());

        return entity;
    }

    public static void updateEntity(
            FmSubscriptionPlan entity,
            FmSubscriptionPlanRequestDto request) {

        entity.setPlanName(request.getPlanName());
        entity.setPrice(request.getPrice());
        entity.setDurationInDays(request.getDurationInDays());
        entity.setBannerDurationInDays(request.getBannerDurationInDays());
        entity.setRadiusInKms(request.getRadiusInKms());
        entity.setBannerSlot(request.getBannerSlot());
        entity.setBestRestaurantSlot(request.getBestRestaurantSlot());
        entity.setDealsSlot(request.getDealsSlot());
        entity.setWhatsappBroadcast(request.getWhatsappBroadcast());
        entity.setVideoCredits(request.getVideoCredits());
        entity.setStateId(request.getStateId());
        entity.setCityId(request.getCityId());
        entity.setAreaId(request.getAreaId());

        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(request.getUserId());
    }

    public static SubscriptionPlanResponseDto toDto(
            FmSubscriptionPlan entity) {

        SubscriptionPlanResponseDto dto =
                new SubscriptionPlanResponseDto();

        dto.setSubscriptionPlanId(entity.getSubscriptionPlanId());
        dto.setPlanName(entity.getPlanName());
        dto.setPrice(entity.getPrice());
        dto.setDurationInDays(entity.getDurationInDays());
        dto.setBannerDurationInDays(entity.getBannerDurationInDays());
        dto.setRadiusInKms(entity.getRadiusInKms());
        dto.setBannerSlot(entity.getBannerSlot());
        dto.setBestRestaurantSlot(entity.getBestRestaurantSlot());
        dto.setDealsSlot(entity.getDealsSlot());
        dto.setWhatsappBroadcast(entity.getWhatsappBroadcast());
        dto.setVideoCredits(entity.getVideoCredits());
        dto.setStateId(entity.getStateId());
        dto.setCityId(entity.getCityId());
        dto.setAreaId(entity.getAreaId());

        return dto;
    }
}