package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class FmOutletDetailsDto {
    private Integer outletId;
    private String outletName;
    private String outletPhone;
    private Boolean isFavourite;
    // Outlet Toggle
    @Schema(example = "true")
    private Boolean isAvailable;

    private List<FmOutletTimingDto> outletTimings;
    private List<FmCategoryDto> categories;
}