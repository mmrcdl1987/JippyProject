package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutletLocationResponseDto {

    private Integer outletId;

    private Double latitude;

    private Double longitude;

    private Integer stateId;

    private Integer cityId;

    private Integer areaId;
}