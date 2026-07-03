package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.CustomerBannerDto;
import com.jippy.foodandmart.projections.ActiveBannerProjection;

public final class CustomerBannerMapper {

    private CustomerBannerMapper() {
    }

    public static CustomerBannerDto toDto(
            ActiveBannerProjection projection,
            String bannerType,
            Integer slotNumber,
            String bannerUrl) {

        CustomerBannerDto dto = new CustomerBannerDto();

        dto.setAreaId(projection.getAreaId());

        dto.setOutletId(projection.getOutletId());

        dto.setOutletName(projection.getOutletName());

        dto.setSlotNumber(slotNumber);

        dto.setBannerType(bannerType);

        dto.setBannerUrl(bannerUrl);

        dto.setPriceModelType(projection.getPriceModelType());

        dto.setOfferAmount(projection.getOfferAmount());

        return dto;
    }
}