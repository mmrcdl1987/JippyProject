package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "ApplicationVersionResponse",
        description = "Application version and application update information"
)
public class ApplicationVersionResponseDTO {

    @Schema(
            description = "Application settings primary key",
            example = "3"
    )
    private Long app_settings_id;

    @Schema(
            description = "Application name",
            example = "JippyMart Driver"
    )
    private String app_name;

    @Schema(
            description = "Application package name",
            example = "com.jippymart.driver"
    )
    private String package_name;

    @Schema(
            description = "Application type",
            example = "driver"
    )
    private String app_type;

    @Schema(
            description = "Latest Android application version",
            example = "2.2.6"
    )
    private String android_version;

    @Schema(
            description = "Android application build number",
            example = "14"
    )
    private String android_build;

    @Schema(
            description = "Latest iOS application version",
            example = "2.2.6"
    )
    private String ios_version;

    @Schema(
            description = "iOS application build number",
            example = "24"
    )
    private String ios_build;

    @Schema(
            description = "Minimum application version required",
            example = "2.2.6"
    )
    private String min_required_version;

    @Schema(
            description = "Latest available application version",
            example = "2.2.6"
    )
    private String latest_version;

    @Schema(
            description = "Google Play Store application URL",
            example = "https://play.google.com/store/apps/details?id=com.jippymart.driver"
    )
    private String googlePlayLink;

    @Schema(
            description = "Apple App Store application URL",
            example = "https://apps.apple.com/in/app/jippymart/id6755134966"
    )
    private String appStoreLink;

    @Schema(
            description = "Indicates whether force update is enabled",
            example = "true"
    )
    private Boolean force_update;

    @Schema(
            description = "Last time application settings were updated",
            example = "2026-09-03T10:52:16.855191"
    )
    private String last_updated;

    @Schema(
            description = "Minimum application version used by the update check",
            example = "2.2.6"
    )
    private String min_app_version;
//
//    @Schema(
//            description = "Indicates whether the update should be shown",
//            example = "true"
//    )
//    private Boolean show_update;
}