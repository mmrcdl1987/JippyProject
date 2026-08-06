package com.jippy.division.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DivCampaignRequestDto {

    private Integer couponId;

    private String campainType;

    private Integer priceModelId;

    private Double priceDropValue;

    // STATE / CITY / AREA
    private Integer locationId;

    // STATE / CITY / AREA
    private String locationType;

    private List<Integer> outletIds;

    private List<Integer> productIds;

    private String promotionFromDate;

    private String promotionToDate;

    private Integer createdBy; // Stores numeric User ID (e.g. 101)

    // Supports multiple meal slots sent from Frontend
    private List<Integer> mealTypeSlotIds;

    // Single meal slot ID used internally during processing/saving
    private Integer mealTypeSlotId;
}