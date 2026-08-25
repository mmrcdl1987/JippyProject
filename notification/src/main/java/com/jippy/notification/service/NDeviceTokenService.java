package com.jippy.notification.service;

import com.jippy.notification.dto.NDeviceTokenRequest;
import com.jippy.notification.dto.NApiResponse;

/**
 * Service Interface for Device Token Operations.
 */
public interface NDeviceTokenService {

    /**
     * Save or Update FCM Device Token.
     *
     * @param request Device Token Request
     * @return API Response
     */
    NApiResponse saveDeviceToken(NDeviceTokenRequest request);

}