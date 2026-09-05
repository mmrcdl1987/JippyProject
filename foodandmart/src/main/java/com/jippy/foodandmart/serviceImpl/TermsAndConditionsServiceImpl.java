package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.TermsAndConditionsResponseDTO;
import com.jippy.foodandmart.entity.TermsAndConditions;
import com.jippy.foodandmart.mapper.TermsAndConditionsMapper;
import com.jippy.foodandmart.repository.TermsAndConditionsRepository;
import com.jippy.foodandmart.service.TermsAndConditionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TermsAndConditionsServiceImpl
        implements TermsAndConditionsService {

    private final TermsAndConditionsRepository
            termsAndConditionsRepository;


    /*
     * ============================================================
     * GET TERMS AND CONDITIONS / PRIVACY POLICY
     * ============================================================
     *
     * appType:
     *
     * customer
     * merchant
     * driver
     *
     * appPolicyType:
     *
     * TERMSANDCONDITIONS
     * PRIVACYPOLICY
     *
     * Both parameters are case-insensitive.
     */
    @Override
    public TermsAndConditionsResponseDTO
    getTermsAndConditionsForAppType(
            String appType,
            String appPolicyType) {

        log.info(
                "Fetching application policy. appType: {}, appPolicyType: {}",
                appType,
                appPolicyType
        );


        /*
         * ========================================================
         * VALIDATE APP TYPE
         * ========================================================
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
         * " merchant " -> "merchant"
         */
        String normalizedAppType =
                appType.trim();


        /*
         * Validate supported app types.
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
         * ========================================================
         * VALIDATE APP POLICY TYPE
         * ========================================================
         */

        if (appPolicyType == null
                || appPolicyType.trim().isEmpty()) {

            log.warn(
                    "App policy type is null or empty"
            );

            throw new IllegalArgumentException(
                    "App policy type must not be null or empty"
            );
        }


        /*
         * Remove unnecessary spaces.
         *
         * Example:
         *
         * " PRIVACYPOLICY " -> "PRIVACYPOLICY"
         */
        String normalizedAppPolicyType =
                appPolicyType.trim();


        /*
         * Validate supported policy types.
         */
        if (!normalizedAppPolicyType.equalsIgnoreCase(
                FmAppConstants.POLICY_TYPE_TERMS_AND_CONDITIONS)
                && !normalizedAppPolicyType.equalsIgnoreCase(
                FmAppConstants.POLICY_TYPE_PRIVACY_POLICY)) {

            log.warn(
                    "Invalid appPolicyType received: {}",
                    appPolicyType
            );

            throw new IllegalArgumentException(
                    "Invalid app policy type. Supported values are " +
                            "TERMSANDCONDITIONS and PRIVACYPOLICY"
            );
        }


        /*
         * ========================================================
         * FETCH DATABASE RECORD
         * ========================================================
         *
         * Only appType is required for the database lookup.
         *
         * appPolicyType determines which column will be returned.
         */
        TermsAndConditions entity =
                termsAndConditionsRepository
                        .findByAppTypeIgnoreCase(
                                normalizedAppType
                        )
                        .orElseThrow(() -> {

                            log.error(
                                    "Terms and conditions record not found " +
                                            "for appType: {}",
                                    normalizedAppType
                            );

                            return new RuntimeException(
                                    "Application policy details not found " +
                                            "for app type: "
                                            + normalizedAppType
                            );
                        });


        log.info(
                "Application policy record found. " +
                        "id: {}, appType: {}",
                entity.getTermsAndConditionsId(),
                entity.getAppType()
        );


        /*
         * ========================================================
         * MAP RESPONSE
         * ========================================================
         *
         * Mapper decides which column to return:
         *
         * TERMSANDCONDITIONS
         *      -> terms_and_conditions
         *
         * PRIVACYPOLICY
         *      -> privacy_and_policy
         */
        TermsAndConditionsResponseDTO response =
                TermsAndConditionsMapper.mapToResponse(
                        entity,
                        normalizedAppPolicyType
                );


        log.info(
                "Application policy details successfully prepared. " +
                        "appType: {}, appPolicyType: {}",
                normalizedAppType,
                normalizedAppPolicyType
        );


        return response;
    }
}