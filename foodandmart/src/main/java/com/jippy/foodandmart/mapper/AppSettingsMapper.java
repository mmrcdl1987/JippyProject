package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.ApplicationVersionResponseDTO;
import com.jippy.foodandmart.dto.ApplicationVersionUpdateRequestDTO;
import com.jippy.foodandmart.dto.ApplicationVersionUpdateResponseDTO;
import com.jippy.foodandmart.entity.AppSettings;

public class AppSettingsMapper {

    /*
     * Updates only the fields which are allowed
     * to be changed through the update API.
     *
     * These fields are intentionally NOT updated:
     *
     * appSettingsId
     * appName
     * packageName
     * appType
     * lastUpdated
     */
    public static void updateApplicationVersion(AppSettings appSettings, ApplicationVersionUpdateRequestDTO requestDTO) {

        appSettings.setAndroidVersion(requestDTO.getAndroid_version());

        appSettings.setAndroidBuild(requestDTO.getAndroid_build());

        appSettings.setIosVersion(requestDTO.getIos_version());

        appSettings.setIosBuild(requestDTO.getIos_build());

        appSettings.setMinRequiredVersion(requestDTO.getMin_required_version());

        appSettings.setLatestVersion(requestDTO.getLatest_version());

        appSettings.setAndroidUpdateUrl(requestDTO.getAndroid_update_url());

        appSettings.setIosUpdateUrl(requestDTO.getIos_update_url());

        appSettings.setForceUpdate(requestDTO.getForce_update());
    }


    /*
     * Converts the updated AppSettings entity
     * into the response DTO.
     *
     * appSettingsId is intentionally not included
     * in the response.
     */
    public static ApplicationVersionUpdateResponseDTO mapToApplicationVersionUpdateResponse(AppSettings appSettings) {

        ApplicationVersionUpdateResponseDTO response = new ApplicationVersionUpdateResponseDTO();

        response.setApp_name(appSettings.getAppName());

        response.setPackage_name(appSettings.getPackageName());

        response.setApp_type(appSettings.getAppType());

        response.setAndroid_version(appSettings.getAndroidVersion());

        response.setAndroid_build(appSettings.getAndroidBuild());

        response.setIos_version(appSettings.getIosVersion());

        response.setIos_build(appSettings.getIosBuild());

        response.setMin_required_version(appSettings.getMinRequiredVersion());

        response.setLatest_version(appSettings.getLatestVersion());

        /*
         * Android Play Store URL
         */
        response.setAndroid_update_url(appSettings.getAndroidUpdateUrl());

        /*
         * Apple App Store URL
         */
        response.setIos_update_url(appSettings.getIosUpdateUrl());

        response.setForce_update(appSettings.getForceUpdate());

        /*
         * last_updated is generated from Java code
         * using LocalDateTime.now() in the service.
         */
        if (appSettings.getLastUpdated() != null) {

            response.setLast_updated(appSettings.getLastUpdated().toString());
        }

        return response;
    }

    /*
     * Converts AppSettings entity into
     * ApplicationVersionResponseDTO.
     *
     * This method is used by the GET API.
     *
     * app_settings_id is included because
     * the GET response now needs the primary key.
     *
     * googlePlayLink comes from android_update_url.
     *
     * appStoreLink comes from ios_update_url.
     *
     * min_app_version comes from min_required_version.
     *
     * show_update follows force_update because
     * this API receives only appType and does not
     * receive the currently installed app version.
     */
    public static ApplicationVersionResponseDTO mapToApplicationVersionResponse(AppSettings appSettings) {

        ApplicationVersionResponseDTO response = new ApplicationVersionResponseDTO();

        response.setApp_settings_id(appSettings.getAppSettingsId());

        response.setApp_name(appSettings.getAppName());

        response.setPackage_name(appSettings.getPackageName());

        response.setApp_type(appSettings.getAppType());

        response.setAndroid_version(appSettings.getAndroidVersion());

        response.setAndroid_build(appSettings.getAndroidBuild());

        response.setIos_version(appSettings.getIosVersion());

        response.setIos_build(appSettings.getIosBuild());

        response.setMin_required_version(appSettings.getMinRequiredVersion());

        response.setLatest_version(appSettings.getLatestVersion());

        /*
         * Database:
         * android_update_url
         *
         * Response:
         * googlePlayLink
         */
        response.setGooglePlayLink(appSettings.getAndroidUpdateUrl());

        /*
         * Database:
         * ios_update_url
         *
         * Response:
         * appStoreLink
         */
        response.setAppStoreLink(appSettings.getIosUpdateUrl());

        response.setForce_update(appSettings.getForceUpdate());

        /*
         * last_updated is returned as String
         * in the response DTO.
         */
        if (appSettings.getLastUpdated() != null) {

            response.setLast_updated(appSettings.getLastUpdated().toString());
        }

        /*
         * Derived field.
         *
         * min_app_version comes from
         * min_required_version.
         */
        response.setMin_app_version(appSettings.getMinRequiredVersion());

        /*
         * Since the GET API only receives appType,
         * there is no installed/current version
         * available for comparison.
         *
         * Therefore show_update follows force_update.
         */
//        response.setShow_update(appSettings.getForceUpdate());

        return response;
    }
}