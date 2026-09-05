package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "TermsAndConditionsResponse",
        description = "Terms and Conditions or Privacy Policy details for an application type"
)
public class TermsAndConditionsResponseDTO {

    @Schema(
            description = "Primary key of the terms and conditions record",
            example = "2"
    )
    private Long terms_and_conditions_id;

    @Schema(
            description = "Application type",
            example = "merchant"
    )
    private String app_type;

    @Schema(
            description = "Terms and Conditions or Privacy Policy content based on appPolicyType",
            example = "These are the Terms and Conditions for JippyMart merchants."
    )
    private String content;
}