package com.jippy.foodandmart.dto;

import com.jippy.foodandmart.enums.FmOtpUserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyLoginOtpRequestDto {

    @NotNull(message = "User type is required")
    private FmOtpUserType userType;

    @NotBlank(message = "Mobile number is required")
    @Pattern(
            regexp = "^[6-9][0-9]{9}$",
            message = "Invalid Indian mobile number"
    )
    private String mobileNumber;

    @NotBlank(message = "OTP is required")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "OTP must be 6 digits"
    )
    private String otp;
}