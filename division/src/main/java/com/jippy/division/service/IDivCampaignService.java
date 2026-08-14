package com.jippy.division.service;

import com.jippy.division.dto.*;

import java.util.List;

public interface IDivCampaignService {

    /**
     * Create Coupon / Price Drop Campaign
     */
    String createCampaign(DivCampaignRequestDto dto);

    /**
     * Get Available Outlets
     */
    List<DivOutletDto> getAvailableOutlets(Integer areaId);

    /**
     * Get Available Meal Slots
     */
    List<AvailableMealSlotResponseDto> getAvailableMealSlots(
            AvailableMealSlotRequestDto request);

    String updateCampaign(Integer campaignId, DivCampaignRequestDto dto);

    String deleteCampaign(String campaignType, Integer campaignId);


    List<DivActiveDiscountsResponseDto> getActiveDiscounts();
}