package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.FmPublicNearbyOutletDto;
import com.jippy.foodandmart.projections.FmPublicNearbyOutletProjection;

import java.util.List;

public final class FmPublicNearbyOutletMapper {

    private FmPublicNearbyOutletMapper() {
    }

    public static FmPublicNearbyOutletDto toDto(FmPublicNearbyOutletProjection projection) {
        FmPublicNearbyOutletDto dto = new FmPublicNearbyOutletDto();
        dto.setOutletId(projection.getOutletId());
        dto.setOutletName(projection.getOutletName());
        dto.setMerchantId(projection.getMerchantId());
        dto.setOutletPhone(projection.getOutletPhone());
        dto.setRadius(projection.getRadius());
        dto.setSubscriptionStatus(projection.getSubscriptionStatus());
        dto.setPromotionStatus(projection.getPromotionStatus());
        dto.setReview(projection.getReview());
        dto.setIsActive(projection.getIsActive());
        dto.setIsApproved(projection.getIsApproved());
        dto.setDistanceKm(projection.getDistanceInKm());
        return dto;
    }

    public static List<FmPublicNearbyOutletDto> toDtoList(List<FmPublicNearbyOutletProjection> projections) {
        return projections.stream().map(FmPublicNearbyOutletMapper::toDto).toList();
    }
}
