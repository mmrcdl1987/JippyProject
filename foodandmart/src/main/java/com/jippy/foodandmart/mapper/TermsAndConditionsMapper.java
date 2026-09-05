package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.TermsAndConditionsResponseDTO;
import com.jippy.foodandmart.entity.TermsAndConditions;

public class TermsAndConditionsMapper {

    /*
     * Converts the entity into the response DTO.
     *
     * The content returned depends on appPolicyType:
     *
     * TERMSANDCONDITIONS
     *      -> terms_and_conditions
     *
     * PRIVACYPOLICY
     *      -> privacy_and_policy
     */
    public static TermsAndConditionsResponseDTO
    mapToResponse(
            TermsAndConditions entity,
            String appPolicyType) {

        TermsAndConditionsResponseDTO response =
                new TermsAndConditionsResponseDTO();

        /*
         * Primary key.
         */
        response.setTerms_and_conditions_id(
                entity.getTermsAndConditionsId()
        );

        /*
         * Application type.
         */
        response.setApp_type(
                entity.getAppType()
        );

        /*
         * Determine which database column
         * should be returned.
         */
        if (appPolicyType.equalsIgnoreCase(
                FmAppConstants.POLICY_TYPE_TERMS_AND_CONDITIONS)) {

            response.setContent(
                    entity.getTermsAndConditions()
            );

        } else if (appPolicyType.equalsIgnoreCase(
                FmAppConstants.POLICY_TYPE_PRIVACY_POLICY)) {

            response.setContent(
                    entity.getPrivacyAndPolicy()
            );
        }

        return response;
    }
}