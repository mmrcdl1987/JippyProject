package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.GroupedBannerResponseDto;

public interface BannerCacheService {

    void refreshBannerCache();

    public GroupedBannerResponseDto getActiveBannersForLocation(double lat, double lng);

}