package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmApprovalSettingsRequestDTO;
import com.jippy.foodandmart.dto.FmApprovalSettingsResponseDTO;

public interface IFmApprovalSettingsService {

    FmApprovalSettingsResponseDTO createApproval(FmApprovalSettingsRequestDTO requestDTO);

}