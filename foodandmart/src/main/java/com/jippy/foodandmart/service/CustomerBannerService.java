package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.AreaBannerCacheDto;

public interface CustomerBannerService {

    AreaBannerCacheDto getCustomerBanners(Integer areaId);

}