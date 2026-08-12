package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmProductPriceSettingsRequestDto;
import com.jippy.foodandmart.dto.FmProductPriceSettingsResponseDto;
import com.jippy.foodandmart.entity.FmProductPriceSettings;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FmProductPriceSettingsMapper {

    public FmProductPriceSettings toEntity(
            FmProductPriceSettingsRequestDto dto,
            Integer userId,
            LocalDateTime now) {

        FmProductPriceSettings entity = new FmProductPriceSettings();

        entity.setOutletId(dto.getOutletId());
        entity.setProductId(dto.getProductId());
        entity.setProductVariantId(dto.getProductVariantId());
        entity.setStartDateTime(dto.getStartDateTime());
        entity.setEndDateTime(dto.getEndDateTime());
        entity.setPriceValue(dto.getPriceValue());
        entity.setPriceType(dto.getPriceType());
        entity.setPriceAdjustmentType(dto.getPriceAdjustmentType());
        entity.setLocationId(dto.getLocationId());
        entity.setLocationType(dto.getLocationType());

        entity.setCreatedBy(userId);
        entity.setCreatedAt(now);

        return entity;
    }

    public void updateEntity(
            FmProductPriceSettings entity,
            FmProductPriceSettingsRequestDto dto,
            Integer userId,
            LocalDateTime now) {

        /*
         * Immutable fields:
         * outletId
         * productId
         * productVariantId
         *
         * These identify what the price setting belongs to
         * and must not be changed during update.
         */

        entity.setStartDateTime(dto.getStartDateTime());
        entity.setEndDateTime(dto.getEndDateTime());
        entity.setPriceValue(dto.getPriceValue());
        entity.setPriceType(dto.getPriceType());
        entity.setPriceAdjustmentType(dto.getPriceAdjustmentType());
        entity.setLocationId(dto.getLocationId());
        entity.setLocationType(dto.getLocationType());

        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(now);
    }

    public FmProductPriceSettingsResponseDto toDto(
            FmProductPriceSettings entity) {

        return new FmProductPriceSettingsResponseDto(
                entity.getProductPriceSettingsId(),
                entity.getOutletId(),
                entity.getProductId(),
                entity.getProductVariantId(),
                entity.getStartDateTime(),
                entity.getEndDateTime(),
                entity.getPriceValue(),
                entity.getPriceType(),
                entity.getPriceAdjustmentType(),
                entity.getLocationId(),
                entity.getLocationType(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedAt()
        );
    }
}