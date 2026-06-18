package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmSubscriptionPlanDto;
import com.jippy.foodandmart.dto.SubscriptionPlanResponseDto;
import com.jippy.foodandmart.entity.FmSubscriptionPlan;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;


@Component
public class FmSubscriptionPlanMapper {

    // Convert DTO → Entity
    public static FmSubscriptionPlan mapToEntity(FmSubscriptionPlanDto subPlanDto) {

        FmSubscriptionPlan entity = new FmSubscriptionPlan();

        entity.setPlanName(subPlanDto.getPlanName());
        entity.setPrice(subPlanDto.getPrice());
        entity.setDurationInDays(subPlanDto.getDurationInDays());
        entity.setRadiusInKms(subPlanDto.getRadiusInKms());
        entity.setBannerSlots(subPlanDto.getBannerSlots());
        entity.setBannerDays(subPlanDto.getBannerDays());
        entity.setBestRestaurantSlot(subPlanDto.getBestRestaurantSlot());
        entity.setWhatsappBroadcast(subPlanDto.getWhatsappBroadcast());
        entity.setVideoCredits(subPlanDto.getVideoCredits());
        entity.setStateId(subPlanDto.getStateId());
        entity.setCityId(subPlanDto.getCityId());
        entity.setAreaId(subPlanDto.getAreaId());

        return entity;
    }

    // Convert Entity → DTO
    public static FmSubscriptionPlanDto mapToDto(FmSubscriptionPlan subPlanEntity) {

        FmSubscriptionPlanDto dto = new FmSubscriptionPlanDto();

        dto.setSubscriptionPlanId(subPlanEntity.getSubscriptionPlanId());
        dto.setPlanName(subPlanEntity.getPlanName());
        dto.setPrice(subPlanEntity.getPrice());
        dto.setDurationInDays(subPlanEntity.getDurationInDays());
        dto.setRadiusInKms(subPlanEntity.getRadiusInKms());
        dto.setBannerSlots(subPlanEntity.getBannerSlots());
        dto.setBannerDays(subPlanEntity.getBannerDays());
        dto.setBestRestaurantSlot(subPlanEntity.getBestRestaurantSlot());
        dto.setWhatsappBroadcast(subPlanEntity.getWhatsappBroadcast());
        dto.setVideoCredits(subPlanEntity.getVideoCredits());
        dto.setStateId(subPlanEntity.getStateId());
        dto.setCityId(subPlanEntity.getCityId());
        dto.setAreaId(subPlanEntity.getAreaId());

        return dto;
    }

    // Update existing entity from DB with new values from DTO
    public static void updateEntity(FmSubscriptionPlan entity, FmSubscriptionPlanDto dto) {

        entity.setPlanName(dto.getPlanName());
        entity.setPrice(dto.getPrice());
        entity.setDurationInDays(dto.getDurationInDays());
        entity.setRadiusInKms(dto.getRadiusInKms());
        entity.setBannerSlots(dto.getBannerSlots());
        entity.setBannerDays(dto.getBannerDays());
        entity.setBestRestaurantSlot(dto.getBestRestaurantSlot());
        entity.setWhatsappBroadcast(dto.getWhatsappBroadcast());
        entity.setVideoCredits(dto.getVideoCredits());
        entity.setStateId(dto.getStateId());
        entity.setCityId(dto.getCityId());
        entity.setAreaId(dto.getAreaId());

        entity.setUpdatedAt(LocalDateTime.now());
    }

    public SubscriptionPlanResponseDto toResponseDto(FmSubscriptionPlan entity) {

        if (entity == null) {
            return null;
        }

        SubscriptionPlanResponseDto dto = new SubscriptionPlanResponseDto();

        dto.setSubscriptionPlanId(entity.getSubscriptionPlanId());
        dto.setPlanName(entity.getPlanName());
        dto.setPrice(entity.getPrice());
        dto.setDurationInDays(entity.getDurationInDays());
        dto.setRadiusInKms(entity.getRadiusInKms());
        dto.setBannerSlots(entity.getBannerSlots());
        dto.setBannerDays(entity.getBannerDays());
        dto.setBestRestaurantSlot(entity.getBestRestaurantSlot());
        dto.setWhatsappBroadcast(entity.getWhatsappBroadcast());
        dto.setVideoCredits(entity.getVideoCredits());

        return dto;
    }
}