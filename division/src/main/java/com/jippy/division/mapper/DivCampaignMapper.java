package com.jippy.division.mapper;

import com.jippy.division.dto.DivCampaignRequestDto;
import com.jippy.division.dto.DivCampaignSlotDto;
import com.jippy.division.entity.DivCouponMappingOutletProduct;
import com.jippy.division.entity.DivPriceDropMappingOutletProduct;
import com.jippy.division.entity.DivPromotionDate;
import com.jippy.division.entity.DivPromotionTime;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class DivCampaignMapper {

    private DivCampaignMapper() {
    }

    /**
     * MAP PROMOTION DATE
     */
    public static DivPromotionDate mapToPromotionDateEntity(DivCampaignRequestDto dto) {

        DivPromotionDate promotionDate = new DivPromotionDate();

        promotionDate.setPromotionFromDate(LocalDateTime.parse(dto.getPromotionFromDate()));

        promotionDate.setPromotionToDate(LocalDateTime.parse(dto.getPromotionToDate()));

        promotionDate.setNoOfSlots(dto.getSlots().size());

        promotionDate.setCreatedAt(LocalDateTime.now());

        promotionDate.setCreatedBy(dto.getCreatedBy());

        return promotionDate;
    }

    /**
     * MAP PROMOTION TIME
     */
    public static DivPromotionTime mapToPromotionTimeEntity(DivCampaignSlotDto slot, Integer promotionDateId, Integer createdBy) {

        DivPromotionTime promotionTime = new DivPromotionTime();

        promotionTime.setSlotNumber(slot.getSlotNumber());

        promotionTime.setPromotionFromTime(LocalTime.parse(slot.getPromotionFromTime()));

        promotionTime.setPromotionToTime(LocalTime.parse(slot.getPromotionToTime()));

        promotionTime.setPromotionDateId(promotionDateId);

        promotionTime.setCreatedAt(LocalDateTime.now());

        promotionTime.setCreatedBy(createdBy);

        return promotionTime;
    }

    /**
     * MAP COUPON ENTITY
     */
    public static DivCouponMappingOutletProduct mapToCouponMappingEntity(Integer couponId, Integer outletId, Integer productId, Integer areaId, Integer promotionTimeId, Integer createdBy) {

        DivCouponMappingOutletProduct mapping = new DivCouponMappingOutletProduct();

        mapping.setCouponId(couponId);

        mapping.setOutletId(outletId);

        mapping.setProductId(productId);

        mapping.setAreaId(areaId);

        mapping.setPromotionTimeId(promotionTimeId);

        mapping.setCreatedAt(LocalDateTime.now());

        mapping.setCreatedBy(createdBy);

        return mapping;
    }

    /**
     * MAP PRICE DROP ENTITY
     */
    public static DivPriceDropMappingOutletProduct mapToPriceDropEntity(Integer outletId, Integer productId, Integer areaId, Integer promotionTimeId, Integer priceModelId, Double priceDropValue, Integer createdBy) {

        DivPriceDropMappingOutletProduct entity = new DivPriceDropMappingOutletProduct();

        entity.setOutletId(outletId);

        entity.setProductId(productId);

        entity.setAreaId(areaId);

        entity.setPromotionTimeId(promotionTimeId);

        entity.setPriceModelId(priceModelId);

        entity.setPriceDropValue(priceDropValue);

        entity.setCreatedAt(LocalDateTime.now());

        entity.setCreatedBy(createdBy);

        return entity;
    }
}