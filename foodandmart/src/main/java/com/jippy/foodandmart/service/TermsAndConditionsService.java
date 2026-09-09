package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.TermsAndConditionsResponseDTO;

public interface TermsAndConditionsService {

    /*
     * Fetch Terms and Conditions or Privacy Policy
     * based on app type and policy type.
     */
    TermsAndConditionsResponseDTO
    getTermsAndConditionsForAppType(
            String appType,
            String appPolicyType
    );
}