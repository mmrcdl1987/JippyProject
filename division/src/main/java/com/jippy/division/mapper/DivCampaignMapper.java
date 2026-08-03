package com.jippy.division.mapper;

import com.jippy.division.dto.DivCampaignRequestDto;
import com.jippy.division.entity.DivCouponMappingOutletProduct;
import com.jippy.division.entity.DivPriceDropMappingOutletsProduct;
import com.jippy.division.entity.DivPromotionDate;

import java.time.LocalDateTime;

public class DivCampaignMapper {

    private DivCampaignMapper() {
    }

    /**
     * MAP PROMOTION DATE
     */
    public static DivPromotionDate mapToPromotionDateEntity(DivCampaignRequestDto dto) {

        DivPromotionDate promotionDate = new DivPromotionDate();

        promotionDate.setPromotionFromDate(
                LocalDateTime.parse(dto.getPromotionFromDate()));

        promotionDate.setPromotionToDate(
                LocalDateTime.parse(dto.getPromotionToDate()));

        promotionDate.setMealTypeSlotId(dto.getMealTypeSlotId());

        promotionDate.setCreatedAt(LocalDateTime.now());
        promotionDate.setCreatedBy(dto.getCreatedBy());

        return promotionDate;
    }

    /**
     * MAP COUPON ENTITY
     */
    public static DivCouponMappingOutletProduct mapToCouponMappingEntity(
            Integer couponId,
            Integer outletId,
            Integer productId,
            Integer locationId,
            String locationType,
            Integer promotionDateId,
            Integer createdBy) {

        DivCouponMappingOutletProduct mapping =
                new DivCouponMappingOutletProduct();

        mapping.setCouponId(couponId);
        mapping.setOutletId(outletId);
        mapping.setProductId(productId);

        mapping.setLocationId(locationId);
        mapping.setLocationType(locationType);

        mapping.setPromotionDateId(promotionDateId);

        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setCreatedBy(createdBy);

        return mapping;
    }

    /**
     * MAP PRICE DROP ENTITY
     */
    public static DivPriceDropMappingOutletsProduct mapToPriceDropEntity(
            Integer outletId,
            Integer productId,
            Integer locationId,
            String locationType,
            Integer promotionDateId,
            Integer priceModelId,
            Double priceDropValue,
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

        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(createdBy);

        return entity;
    }
}