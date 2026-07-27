package com.jippy.division.mapper;

import com.jippy.division.dto.DivCouponRequestDto;
import com.jippy.division.dto.DivCouponResponseDto;
import com.jippy.division.dto.DivPriceModelDto;
import com.jippy.division.entity.DivCoupon;
import com.jippy.division.entity.DivPriceModel;

import java.util.ArrayList;
import java.util.List;

public class DivCouponMapper {

    private DivCouponMapper() {
    }

    public static DivCoupon toEntity(DivCoupon coupon, DivCouponRequestDto dto) {

        if (dto.getCouponCode() != null) {
            coupon.setCouponCode(dto.getCouponCode());
        }

        if (dto.getApplicationType() != null) {
            coupon.setApplicationType(dto.getApplicationType());
        }

        if (dto.getPriceModelId() != null) {
            coupon.setPriceModelId(dto.getPriceModelId());
        }

        if (dto.getMinOrderValue() != null) {
            coupon.setMinOrderValue(dto.getMinOrderValue());
        }

        if (dto.getDiscountValue() != null) {
            coupon.setDiscountValue(dto.getDiscountValue());
        }

        if (dto.getPaymentMethod() != null) {
            coupon.setPaymentMethod(dto.getPaymentMethod());
        }

        if (dto.getUsageLimitPerUser() != null) {
            coupon.setUsageLimitPerUser(dto.getUsageLimitPerUser());
        }

        if (dto.getStartDate() != null) {
            coupon.setStartTime(dto.getStartDate());
        }

        if (dto.getEndDate() != null) {
            coupon.setEndTime(dto.getEndDate());
        }

        if (dto.getUserType() != null) {
            coupon.setUserType(dto.getUserType());
        }

        return coupon;
    }

    public static DivCouponResponseDto toDto(DivCoupon coupon) {

        if (coupon == null) {
            return null;
        }

        DivCouponResponseDto dto = new DivCouponResponseDto();

        dto.setCouponId(coupon.getCouponId());
        dto.setCouponCode(coupon.getCouponCode());
        dto.setApplicationType(coupon.getApplicationType());
        dto.setPriceModelId(coupon.getPriceModelId());
        dto.setMinOrderValue(coupon.getMinOrderValue());
        dto.setDiscountValue(coupon.getDiscountValue());
        dto.setPaymentMethod(coupon.getPaymentMethod());
        dto.setUsageLimitPerUser(coupon.getUsageLimitPerUser());
        dto.setIsActive(coupon.getIsActive());
        dto.setStartTime(coupon.getStartTime());
        dto.setEndTime(coupon.getEndTime());
        dto.setUserType(coupon.getUserType());

        return dto;
    }

    public static List<DivCouponResponseDto> toDtoList(List<DivCoupon> coupons) {

        List<DivCouponResponseDto> dtoList = new ArrayList<>();

        for (DivCoupon coupon : coupons) {
            dtoList.add(toDto(coupon));
        }

        return dtoList;
    }

    public static List<DivPriceModelDto> toPriceModelDto(List<DivPriceModel> priceModels) {

        List<DivPriceModelDto> dtoList = new ArrayList<>();

        for (DivPriceModel model : priceModels) {

            DivPriceModelDto dto = new DivPriceModelDto();

            dto.setPriceModelId(model.getPriceModelId());
            dto.setPriceModelName(model.getPriceModelName());
            dto.setCreatedBy(model.getCreatedBy());
            dto.setCreatedAt(model.getCreatedAt());

            dtoList.add(dto);
        }

        return dtoList;
    }
}