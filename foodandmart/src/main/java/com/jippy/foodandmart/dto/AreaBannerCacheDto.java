package com.jippy.foodandmart.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AreaBannerCacheDto {

    private Integer areaId;

    private List<CustomerBannerDto> mainBanners = new ArrayList<>();

    private List<CustomerBannerDto> bestRestaurantBanners = new ArrayList<>();

    private List<CustomerBannerDto> dealsBanners = new ArrayList<>();

}