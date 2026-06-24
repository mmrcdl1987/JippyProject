package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FmValidateForgotPasswordOtpRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Schema(
            example = "rohanvadluri8463@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @Pattern(
            regexp = "^(EMPLOYEE|MERCHANT|OUTLET|DRIVER)$",
            message = "User type must be EMPLOYEE, MERCHANT, OUTLET, or DRIVER"
    )
    @Schema(example = "DRIVER",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String userType;

    @NotBlank(message = "OTP is required")
    @Pattern(
            regexp = "^\\d{6}$",
            message = "OTP must contain exactly 6 digits"
    )
    @Schema(example = "654321",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String otp;
}