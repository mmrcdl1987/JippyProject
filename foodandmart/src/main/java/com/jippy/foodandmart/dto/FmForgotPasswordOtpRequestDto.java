package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FmForgotPasswordOtpRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Registered email address",
            example = "rohanvadluri8463@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "User type is required")
    @Schema(description = "Mention User Type",
            example = "DRIVER",
            allowableValues = {
                    "EMPLOYEE",
                    "MERCHANT",
                    "OUTLET",
                    "DRIVER"
            },
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String userType;
}