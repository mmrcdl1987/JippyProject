package com.jippy.division.service;

import com.jippy.division.dto.DivCampaignRequestDto;
import com.jippy.division.dto.DivOutletDto;

import java.util.List;

public interface IDivCampaignService {

    String createCampaign(DivCampaignRequestDto dto);

    List<DivOutletDto> getAvailableOutlets(Integer areaId);
}