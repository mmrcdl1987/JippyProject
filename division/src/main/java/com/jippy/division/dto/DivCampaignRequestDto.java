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

    private String promotionMessage;

    private Integer maxSelection;

    private Integer createdBy;

    /**
     * Meal type slots selected by frontend.
     *
     * Example:
     * [87, 88, 91, 92]
     */
    private List<Integer> mealTypeSlotIds;
}