package com.jippy.foodandmart.dto;

import lombok.Data;

@Data
public class UploadBannerResponseDto {

    private Integer outletSubscriptionPlanId;

    private String mainBannerUrl;

    private String bestRestaurantBannerUrl;

    private String dealsBannerUrl;
}