package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "ApplicationVersionUpdateRequest",
        description = "Request payload for updating application version settings"
)
public class ApplicationVersionUpdateRequestDTO {

    @NotBlank(message = "Android version must not be blank")
    @Schema(
            description = "Android application version",
            example = "2.6.0",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String android_version;

    @NotBlank(message = "Android build must not be blank")
    @Schema(
            description = "Android application build number",
            example = "16",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String android_build;

    @NotBlank(message = "iOS version must not be blank")
    @Schema(
            description = "iOS application version",
            example = "2.6.0",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String ios_version;

    @NotBlank(message = "iOS build must not be blank")
    @Schema(
            description = "iOS application build number",
            example = "26",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String ios_build;

    @NotBlank(message = "Minimum required version must not be blank")
    @Schema(
            description = "Minimum application version required",
            example = "2.5.9",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String min_required_version;

    @NotBlank(message = "Latest version must not be blank")
    @Schema(
            description = "Latest application version",
            example = "2.6.0",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String latest_version;

    @Schema(
            description = "Android application update URL",
            example = "https://play.google.com/store/apps/details?id=com.jippymart.customer"
    )
    private String android_update_url;

    @Schema(
            description = "iOS application update URL",
            example = "https://apps.apple.com/app/jippymart/id123456789"
    )
    private String ios_update_url;

    @NotNull(message = "Force update must not be null")
    @Schema(
            description = "Indicates whether users must force update the application",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean force_update;
}