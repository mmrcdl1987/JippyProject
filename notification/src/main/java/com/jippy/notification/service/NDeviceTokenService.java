package com.jippy.notification.service;

import com.jippy.notification.dto.NApiResponse;
import com.jippy.notification.dto.NDeviceTokenRequest;
public interface NDeviceTokenService {

    /**
     * Save or Update FCM Device Token.
     *
     * @param request Device Token Request
     * @return API Response
     */
    NApiResponse saveDeviceToken(NDeviceTokenRequest request);

    /**
     * Delete FCM Device Token during logout.
     *
     * @param fcmToken FCM device token
     * @return API Response
     */
    NApiResponse deleteDeviceToken(String fcmToken);
}