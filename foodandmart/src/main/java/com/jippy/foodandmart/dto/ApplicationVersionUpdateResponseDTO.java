package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "ApplicationVersionUpdateResponse",
        description = "Response containing updated application settings"
)
public class ApplicationVersionUpdateResponseDTO {

    @Schema(example = "JippyMart Customer")
    private String app_name;

    @Schema(example = "com.jippymart.customer")
    private String package_name;

    @Schema(example = "customer")
    private String app_type;

    @Schema(example = "2.6.0")
    private String android_version;

    @Schema(example = "16")
    private String android_build;

    @Schema(example = "2.6.0")
    private String ios_version;

    @Schema(example = "26")
    private String ios_build;

    @Schema(example = "2.5.9")
    private String min_required_version;

    @Schema(example = "2.6.0")
    private String latest_version;

    @Schema(
            example = "https://play.google.com/store/apps/details?id=com.jippymart.customer"
    )
    private String android_update_url;

    @Schema(
            example = "https://play.ios.com/store/apps/details?id=com.jippymart.customer"
    )
    private String ios_update_url;

    @Schema(example = "true")
    private Boolean force_update;

    @Schema(example = "2026-09-03T22:15:30")
    private String last_updated;
}