package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.ActiveBannerResponseDto;
import com.jippy.foodandmart.dto.CustomerBannerDto;

public final class CustomerBannerMapper {

    private CustomerBannerMapper() {
    }

    public static CustomerBannerDto toDto(
            ActiveBannerResponseDto banner,
            String bannerType,
            Integer slotNumber,
            String bannerUrl) {

        CustomerBannerDto dto = new CustomerBannerDto();

        dto.setAreaId(banner.getAreaId());

        dto.setOutletId(banner.getOutletId());

        dto.setOutletName(banner.getOutletName());

        dto.setSlotNumber(slotNumber);

        dto.setBannerType(bannerType);

        dto.setBannerUrl(bannerUrl);

        dto.setPriceModelType(banner.getPriceModelType());

        dto.setOfferAmount(banner.getOfferAmount());

        // NEW
        dto.setBannerFromDate(banner.getBannerFromDate());

        dto.setBannerToDate(banner.getBannerToDate());

        dto.setMealTypeTimings(banner.getMealTypeTimings());

        dto.setRadiusInKms(banner.getRadiusInKms());

        dto.setMealTypeTimingIds(banner.getMealTypeTimingsIds());

        dto.setLatitude(banner.getLatitude());

        dto.setLongitude(banner.getLongitude());

        return dto;
    }
}