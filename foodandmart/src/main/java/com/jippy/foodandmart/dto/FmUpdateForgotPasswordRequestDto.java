package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FmUpdateForgotPasswordRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Enter Registered email address",
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

    @Schema(
            example = "Rohan@123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "New Password is required")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,20}$",
            message = "Password must contain uppercase, lowercase, number and special character")
    private String newPassword;
}