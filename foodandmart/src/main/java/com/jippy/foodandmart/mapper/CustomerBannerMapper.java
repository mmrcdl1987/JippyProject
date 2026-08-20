package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.ActiveBannerResponseDto;
import com.jippy.foodandmart.dto.BannerInfoDto;
import com.jippy.foodandmart.dto.CustomerBannerDto;

import java.util.ArrayList;
import java.util.List;

public final class CustomerBannerMapper {

    private CustomerBannerMapper() {
    }

    public static CustomerBannerDto toDto(
            ActiveBannerResponseDto banner,
            List<ActiveBannerResponseDto> rows) {

        CustomerBannerDto dto = new CustomerBannerDto();

        dto.setOutletId(banner.getOutletId());
        dto.setOutletName(banner.getOutletName());
       // dto.setAreaId(banner.getAreaId());
        dto.setLatitude(banner.getLatitude());
        dto.setLongitude(banner.getLongitude());
        dto.setRadiusInKms(banner.getRadiusInKms());
        dto.setMealTypeTimingIds(banner.getMealTypeTimingsIds());

        List<BannerInfoDto> bannerList = new ArrayList<>();

        for (ActiveBannerResponseDto row : rows) {
            // Main Slot
            if (row.getMainBannerUrl() != null && row.getBannerSlot() != null) {
                bannerList.add(new BannerInfoDto("MAIN", row.getMainBannerUrl(), row.getBannerSlot(), banner.getOutletId(), banner.getOutletName()));
            }
            // Best Restaurant Slot
            if (row.getBestRestaurantBannerUrl() != null && row.getBestRestaurantSlot() != null) {
                bannerList.add(new BannerInfoDto("BEST_RESTAURANT", row.getBestRestaurantBannerUrl(), row.getBestRestaurantSlot(), banner.getOutletId(), banner.getOutletName()));
            }
            // Deals Slot
            if (row.getDealsBannerUrl() != null && row.getDealsSlot() != null) {
                bannerList.add(new BannerInfoDto("DEALS", row.getDealsBannerUrl(), row.getDealsSlot(), banner.getOutletId(), banner.getOutletName()));
            }
        }

        dto.setBannerInfoDtos(bannerList);
        return dto;
    }
}