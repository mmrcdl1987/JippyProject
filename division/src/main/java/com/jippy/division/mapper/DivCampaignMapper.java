package com.jippy.division.mapper;

import com.jippy.division.dto.DivActiveDiscountsResponseDto;
import com.jippy.division.dto.DivCampaignRequestDto;
import com.jippy.division.entity.DivCouponMappingOutletProduct;
import com.jippy.division.entity.DivPriceDropMappingOutletsProduct;
import com.jippy.division.entity.DivPromotionDate;
import com.jippy.division.projection.DivActiveDiscountsProjection;

import java.time.LocalDateTime;

public final class DivCampaignMapper {

    private DivCampaignMapper() {
    }

    /**
     * Maps campaign request to PromotionDate entity.
     *
     * One PromotionDate record is created for each meal type slot.
     */
    public static DivPromotionDate mapToPromotionDateEntity(
            DivCampaignRequestDto dto,
            Integer mealTypeSlotId) {

        DivPromotionDate promotionDate = new DivPromotionDate();

        promotionDate.setPromotionFromDate(
                LocalDateTime.parse(dto.getPromotionFromDate()));

        promotionDate.setPromotionToDate(
                LocalDateTime.parse(dto.getPromotionToDate()));

        promotionDate.setMealTypeSlotId(mealTypeSlotId);

        promotionDate.setCreatedAt(LocalDateTime.now());
        promotionDate.setCreatedBy(dto.getCreatedBy());

        return promotionDate;
    }

    /**
     * Maps campaign request to Coupon Mapping entity.
     */
    public static DivCouponMappingOutletProduct mapToCouponMappingEntity(
            Integer couponId,
            Integer outletId,
            Integer productId,
            Integer locationId,
            String locationType,
            Integer promotionDateId,
            String promotionMessage,
            Integer maxSelection,
            Integer createdBy) {

        DivCouponMappingOutletProduct mapping =
                new DivCouponMappingOutletProduct();

        mapping.setCouponId(couponId);
        mapping.setOutletId(outletId);
        mapping.setProductId(productId);
        mapping.setLocationId(locationId);
        mapping.setLocationType(locationType);
        mapping.setPromotionDateId(promotionDateId);
        mapping.setPromotionMessage(promotionMessage);

        mapping.setMaxSelection(
                maxSelection == null ? -1 : maxSelection
        );

        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setCreatedBy(createdBy);

        return mapping;
    }

    /**
     * Maps campaign request to Price Drop Mapping entity.
     */
    public static DivPriceDropMappingOutletsProduct mapToPriceDropEntity(
            Integer outletId,
            Integer productId,
            Integer locationId,
            String locationType,
            Integer promotionDateId,
            Integer priceModelId,
            Double priceDropValue,
            String promotionMessage,
            Integer maxSelection,
            Integer createdBy) {

        DivPriceDropMappingOutletsProduct entity =
                new DivPriceDropMappingOutletsProduct();

        entity.setOutletId(outletId);
        entity.setProductId(productId);
        entity.setLocationId(locationId);
        entity.setLocationType(locationType);
        entity.setPromotionDateId(promotionDateId);
        entity.setPriceModelId(priceModelId);
        entity.setPriceDropValue(priceDropValue);
        entity.setPromotionMessage(promotionMessage);

        entity.setMaxSelection(
                maxSelection == null ? -1 : maxSelection
        );

        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(createdBy);

        return entity;
    }

    /**
     * Maps active discount projection to response DTO.
     */
    public static DivActiveDiscountsResponseDto mapToActiveDiscounts(
            DivActiveDiscountsProjection projection,
            DivActiveDiscountsResponseDto responseDto) {

        responseDto.setDiscountAmount(projection.getDiscountAmount());
        responseDto.setEndDateTime(projection.getEndDateTime());
        responseDto.setCouponCode(projection.getCouponCode());
        responseDto.setSourceId(projection.getSourceId());
        responseDto.setSourceType(projection.getSourceType());
        responseDto.setStartDateTime(projection.getStartDateTime());
        responseDto.setPriceType(projection.getpriceModelName());
        responseDto.setMinOrderValue(projection.getMinOrderValue());
        responseDto.setProductId(projection.getProductId());
        responseDto.setOutletId(projection.getOutletId());
        responseDto.setUsageLimitPerUser(projection.getUsageLimitPerUser());
        responseDto.setPromotionScheduleId(
                projection.getPromotionScheduleId());
        responseDto.setMealTypeSlotIdsStr(
                projection.getMealTypeSlotIdsStr());
        responseDto.setPromotionMessage(
                projection.getPromotionMessage());

        responseDto.setMaxSelection(
                projection.getMaxSelection());

        return responseDto;
    }
}