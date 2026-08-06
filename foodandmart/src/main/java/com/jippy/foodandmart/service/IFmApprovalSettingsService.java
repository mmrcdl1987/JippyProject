package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmApprovalSettingsRequestDTO;
import com.jippy.foodandmart.dto.FmApprovalSettingsResponseDTO;
import com.jippy.foodandmart.dto.FmUpdateApprovalSettingsRequestDTO;
import com.jippy.foodandmart.dto.FmUpdateApprovalSettingsResponseDTO;

public interface IFmApprovalSettingsService {

    FmApprovalSettingsResponseDTO createApproval(FmApprovalSettingsRequestDTO requestDTO);

    FmUpdateApprovalSettingsResponseDTO replaceApproverWithAreas(
            FmUpdateApprovalSettingsRequestDTO requestDTO);
}