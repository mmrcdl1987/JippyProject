package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.ApplicationVersionResponseDTO;
import com.jippy.foodandmart.dto.ApplicationVersionUpdateRequestDTO;
import com.jippy.foodandmart.dto.ApplicationVersionUpdateResponseDTO;
import com.jippy.foodandmart.entity.AppSettings;
import com.jippy.foodandmart.mapper.AppSettingsMapper;
import com.jippy.foodandmart.repository.AppSettingsRepository;
import com.jippy.foodandmart.service.AppSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppSettingsServiceImpl implements AppSettingsService {

    private final AppSettingsRepository appSettingsRepository;

    /*
     * ============================================================
     * GET APPLICATION VERSION BY APP TYPE
     * ============================================================
     *
     * Supported app types:
     * customer
     * merchant
     * driver
     *
     * App type is case-insensitive.
     */
    @Override
    public ApplicationVersionResponseDTO getApplicationVersionByAppType(
            String appType) {

        log.info(
                "Fetching application version details for appType: {}",
                appType
        );

        /*
         * Validate appType.
         */
        if (appType == null || appType.trim().isEmpty()) {

            log.warn("App type is null or empty");

            throw new IllegalArgumentException(
                    "App type must not be null or empty"
            );
        }

        /*
         * Remove unnecessary spaces.
         *
         * Example:
         *
         * " customer " -> "customer"
         */
        String normalizedAppType = appType.trim();

        /*
         * Validate supported app types.
         *
         * equalsIgnoreCase() makes the validation
         * case-insensitive.
         */
        if (!normalizedAppType.equalsIgnoreCase(
                FmAppConstants.TYPE_CUSTOMER)
                && !normalizedAppType.equalsIgnoreCase(
                FmAppConstants.TYPE_MERCHANT)
                && !normalizedAppType.equalsIgnoreCase(
                FmAppConstants.TYPE_DRIVER)) {

            log.warn(
                    "Invalid appType received: {}",
                    appType
            );

            throw new IllegalArgumentException(
                    "Invalid app type. Supported values are " +
                            "customer, merchant and driver"
            );
        }

        /*
         * Fetch application settings from database.
         *
         * findByAppTypeIgnoreCase() performs
         * case-insensitive lookup.
         */
        AppSettings appSettings =
                appSettingsRepository
                        .findByAppTypeIgnoreCase(normalizedAppType)
                        .orElseThrow(() -> {

                            log.error(
                                    "Application settings not found for appType: {}",
                                    normalizedAppType
                            );

                            return new RuntimeException(
                                    "Application settings not found for app type: "
                                            + normalizedAppType
                            );
                        });

        log.info(
                "Application settings found. " +
                        "appSettingsId: {}, appType: {}",
                appSettings.getAppSettingsId(),
                appSettings.getAppType()
        );

        /*
         * Convert entity to response DTO
         * using the existing static mapper.
         *
         * No Builder.
         * No Map.
         * No Stream.
         */
        ApplicationVersionResponseDTO response =
                AppSettingsMapper.mapToApplicationVersionResponse(
                        appSettings
                );

        log.info(
                "Application version details successfully prepared " +
                        "for appType: {}",
                normalizedAppType
        );

        return response;
    }


    /*
     * ============================================================
     * UPDATE APPLICATION VERSION BY APP TYPE
     * ============================================================
     *
     * Updates only:
     *
     * android_version
     * android_build
     * ios_version
     * ios_build
     * min_required_version
     * latest_version
     * android_update_url
     * ios_update_url
     * force_update
     *
     * Does NOT update:
     *
     * app_settings_id
     * app_name
     * package_name
     * app_type
     *
     * last_updated is generated automatically by Java code.
     */
    @Override
    public ApplicationVersionUpdateResponseDTO updateApplicationVersionByAppType(String appType, ApplicationVersionUpdateRequestDTO requestDTO) {

        log.info("Updating application version details for appType: {}", appType);

        /*
         * Validate appType.
         */
        if (appType == null || appType.trim().isEmpty()) {

            log.warn("App type is null or empty");

            throw new IllegalArgumentException("App type must not be null or empty");
        }

        /*
         * Remove unnecessary spaces.
         *
         * Example:
         *
         * " customer " -> "customer"
         */
        String normalizedAppType = appType.trim();

        /*
         * Validate supported application types.
         *
         * Only customer, merchant and driver
         * are allowed.
         */
        if (!normalizedAppType.equalsIgnoreCase(FmAppConstants.TYPE_CUSTOMER) && !normalizedAppType.equalsIgnoreCase(FmAppConstants.TYPE_MERCHANT) && !normalizedAppType.equalsIgnoreCase(FmAppConstants.TYPE_DRIVER)) {

            log.warn("Invalid appType received for update: {}", appType);

            throw new IllegalArgumentException("Invalid app type. Supported values are customer, merchant and driver");
        }

        /*
         * Fetch existing application settings
         * using appType.
         *
         * Database lookup is case-insensitive.
         */
        AppSettings appSettings = appSettingsRepository.findByAppTypeIgnoreCase(normalizedAppType).orElseThrow(() -> {

            log.error("Application settings not found for appType: {}", normalizedAppType);

            return new RuntimeException("Application settings not found for app type: " + normalizedAppType);
        });

        log.info("Application settings found. " + "appSettingsId: {}, appType: {}", appSettings.getAppSettingsId(), appSettings.getAppType());

        /*
         * Update ONLY the eight allowed fields.
         *
         * Static mapper method is used here.
         *
         * No mapper object / dependency injection required.
         */
        AppSettingsMapper.updateApplicationVersion(appSettings, requestDTO);

        /*
         * Generate last_updated using Java code.
         *
         * It is NOT accepted from the request body.
         */
        appSettings.setLastUpdated(LocalDateTime.now());

        log.info("Application settings fields updated. " + "appType: {}, lastUpdated: {}", normalizedAppType, appSettings.getLastUpdated());

        /*
         * Save the updated application settings.
         */
        AppSettings updatedAppSettings = appSettingsRepository.save(appSettings);

        log.info("Application version successfully updated. " + "appSettingsId: {}, appType: {}, lastUpdated: {}", updatedAppSettings.getAppSettingsId(), updatedAppSettings.getAppType(), updatedAppSettings.getLastUpdated());

        /*
         * Convert the updated entity into response DTO.
         *
         * app_settings_id is intentionally not returned.
         */
        ApplicationVersionUpdateResponseDTO response = AppSettingsMapper.mapToApplicationVersionUpdateResponse(updatedAppSettings);

        log.info("Application version update response prepared " + "for appType: {}", normalizedAppType);

        return response;
    }
}