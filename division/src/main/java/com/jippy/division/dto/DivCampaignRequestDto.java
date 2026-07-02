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

    private List<Integer> outletIds;

    private List<Integer> productIds;

    private Integer areaId;

    private String promotionFromDate;

    private String promotionToDate;

    private Integer createdBy;

    private List<DivCampaignSlotDto> slots;
}