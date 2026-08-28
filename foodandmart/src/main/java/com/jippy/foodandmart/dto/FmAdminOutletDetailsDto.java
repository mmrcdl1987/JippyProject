package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class FmAdminOutletDetailsDto {

    // =========================================================
    // OUTLET DETAILS
    // =========================================================

    private Integer outletId;

    @Schema(example = "Friends Restaurant")
    private String outletName;

    @Schema(example = "friendsrestaurant@gmail.com")
    private String outletEmail;

    @Schema(example = "6548022339")
    private String outletPhone;

    @Schema(example = "9848022339")
    private String alternateOutletPhone;

    private String outletPicUrl;

    // =========================================================
    // LOCATION
    // =========================================================

    private Double latitude;

    private Double longitude;

    // =========================================================
    // OUTLET STATUS
    // =========================================================

    /**
     * Outlet master active status.
     * Y = Active
     * N = Inactive
     */
    private String isActive;

    /**
     * Admin approval status.
     */
    private Boolean isApproved;

    /**
     * Current outlet availability/toggle status.
     */
    private Boolean isAvailable;

    /**
     * Outlet toggle status.
     */
    private Boolean isToggle;

    /**
     * Indicates whether GST is applicable for this outlet.
     */
    private Boolean isGstApplied;

    // =========================================================
    // BANK DETAILS
    // =========================================================

    private String accountNumber;

    private String ifscCode;

    private String bankName;

    private String accountHolderName;

    // =========================================================
    // ADDRESS
    // =========================================================

    private String buildingNumber;

    private String road;

    private String landmark;

    private Integer cityId;

    private String cityName;

    private Integer stateId;

    private String stateName;

    private Integer areaId;

    private String areaName;

    // =========================================================
    // KYC
    // =========================================================

    private String fssaiNumber;

    private String gstNumber;

    // =========================================================
    // CUISINE
    // =========================================================

    private List<FmCuisineTypeResponseDTO> cuisineTypes;

    // =========================================================
    // OUTLET TIMINGS
    // =========================================================

    private List<FmOutletTimingDto> outletTimings;

    // =========================================================
    // CATEGORIES
    // =========================================================

    private List<FmAdminCategoryDto> categories;
}