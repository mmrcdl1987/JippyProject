package com.jippy.division.service;

import com.jippy.division.dto.AvailableMealSlotRequestDto;
import com.jippy.division.dto.AvailableMealSlotResponseDto;
import com.jippy.division.dto.DivCampaignRequestDto;
import com.jippy.division.dto.DivOutletDto;

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


}