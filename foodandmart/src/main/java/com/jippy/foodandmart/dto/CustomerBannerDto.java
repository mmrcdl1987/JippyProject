package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerBannerDto {

    //private Integer areaId;

    private Integer outletId;

    private String outletName;

//    private String priceModelType;
//
//    private BigDecimal offerAmount;
//
    private BigDecimal radiusInKms;

    private Double latitude;

    private Double longitude;

    private Integer[] mealTypeTimingIds;
//
//    // Banner validity
//    private LocalDate bannerFromDate;
//
//    private LocalDate bannerToDate;
//
//    // Meal timings
//    private List<MealTypeTimingResponseDto> mealTypeTimings;

    private List<BannerInfoDto> bannerInfoDtos;
}