package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupedBannerResponseDto {

    private List<BannerInfoDto> mainBannerInfoDtos;
    private List<BannerInfoDto> bestRestaurantBannerInfoDtos;
    private List<BannerInfoDto> dealsBannerInfoDtos;
}
