package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.ApplicationVersionResponseDTO;
import com.jippy.foodandmart.dto.ApplicationVersionUpdateRequestDTO;
import com.jippy.foodandmart.dto.ApplicationVersionUpdateResponseDTO;

public interface AppSettingsService {

    ApplicationVersionResponseDTO getApplicationVersionByAppType(String appType);

    ApplicationVersionUpdateResponseDTO updateApplicationVersionByAppType(
            String appType,
            ApplicationVersionUpdateRequestDTO requestDTO);

}