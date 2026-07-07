package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class FmOutletDetailsDto {
    private Integer outletId;

    @Schema(example = "Friends Restaurant")
    private String outletName;
    @Schema(example = "friendsrestaurant@gmail.com")
    private String outletEmail;
    @Schema(example = "123 Main Street, City, State, ZIP")
    private String outletPhone;

    @Schema(example = "9848022339")
    private String alternateOutletPhone;

    @Schema(example = "Indian")
    private String cuisineType;

    @Schema(example = "17.4940")
    private Double latitude;

    @Schema(example = "78.3990")
    private Double longitude;

    // --------------------------------------------
    //    outlet bank details from  user_bank details table
    @Schema(example = "987654321012")
    private String accountNumber;
    @Schema(example = "SBIN0001234")
    private String ifscCode;
    @Schema(example = "State Bank of India")
    private String bankName;
    @Schema(example = "Friends Restaurant")
    private String accountHolderName;
    //    --------------------------------------------
    // Address Details --- via area, city, state tables for names of area, city, state
    @Schema(example = "10-1-20")
    private String buildingNumber;

    @Schema(example = "Main Road")
    private String road;

    @Schema(example = "Near Metro Station")
    private String landmark;

    @Schema(example = "3")
    private Integer cityId;

    @Schema(example = "Hyderabad")
    private String cityName;

    @Schema(example = "2")
    private Integer stateId;

    @Schema(example = "TELANGANA")
    private String stateName;

    @Schema(example = "13")
    private Integer areaId;

    @Schema(example = "Kukatpally")
    private String areaName;

    // Outlet Toggle
    private Boolean isFavourite;
    @Schema(example = "true")
    private Boolean isAvailable;

    private List<FmOutletTimingDto> outletTimings;
    private List<FmCategoryDto> categories;
}