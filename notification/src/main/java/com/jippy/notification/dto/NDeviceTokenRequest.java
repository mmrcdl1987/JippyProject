package com.jippy.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for Saving Device Token")
public class NDeviceTokenRequest {

    @NotNull(message = "User Id is required")
    @Schema(description = "User Id", example = "101")
    private Integer userId;

    @NotBlank(message = "User Type is required")
    @Size(max = 30)
    @Schema(description = "User Type", example = "CUSTOMER")
    private String userType;

    @NotBlank(message = "Device Type is required")
    @Size(max = 30)
    @Schema(description = "Device Type", example = "ANDROID")
    private String deviceType;

    @NotBlank(message = "FCM Token is required")
    @Size(max = 500)
    @Schema(description = "Firebase Cloud Messaging Token")
    private String fcmToken;

}