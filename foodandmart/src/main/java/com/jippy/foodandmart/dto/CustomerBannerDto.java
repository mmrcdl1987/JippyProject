package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBannerDto {

    private Integer areaId;

    private Integer outletId;

    private String outletName;

    private Integer slotNumber;

    private String bannerType;

    private String bannerUrl;

    private String priceModelType;

    private BigDecimal offerAmount;
}