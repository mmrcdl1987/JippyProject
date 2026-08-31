package com.jippy.foodandmart.dto;

import lombok.Data;

import java.util.List;

@Data
public class FmPublicNearbyOutletResponseDto {

    private Integer areaId;
    private Integer totalOutlets;
    private List<FmPublicNearbyOutletDto> outlets;
}
