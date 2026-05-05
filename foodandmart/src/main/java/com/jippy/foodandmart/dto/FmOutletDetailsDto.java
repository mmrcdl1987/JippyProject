package com.jippy.foodandmart.dto;

import lombok.Data;

import java.util.List;

@Data
public class FmOutletDetailsDto {
    private Integer outletId;
    private String outletName;
    private String outletPhone;

    private List<FmOutletTimingDto> outletTimings;
    private List<FmCategoryDto> categories;
}