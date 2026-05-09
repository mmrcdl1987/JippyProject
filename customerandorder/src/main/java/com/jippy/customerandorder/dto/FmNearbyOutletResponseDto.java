package com.jippy.customerandorder.dto;

import lombok.Data;

import java.util.List;

@Data
public class FmNearbyOutletResponseDto {

    private Integer areaId;
    private Integer totalOutlets;
    private List<FmOutletDto> outlets;
}