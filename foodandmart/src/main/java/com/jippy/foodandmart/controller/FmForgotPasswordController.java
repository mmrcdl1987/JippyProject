package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmForgotPasswordOtpRequestDto;
import com.jippy.foodandmart.dto.FmForgotPasswordResponseDto;
import com.jippy.foodandmart.dto.FmUpdateForgotPasswordRequestDto;
import com.jippy.foodandmart.dto.FmValidateForgotPasswordOtpRequestDto;
import com.jippy.foodandmart.service.FmForgotPasswordService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/fm")
public class FmForgotPasswordController {

    private final FmForgotPasswordService fmForgotPasswordService;

    @PostMapping("/forgetPasswordForUserTypeBySendingOtpToMail")
    @Operation(summary = "Forgot Password OTP",
            description = "Verifies email based on user type and sends OTP to registered email")
    public ResponseEntity<FmForgotPasswordResponseDto> forgetPasswordForUserTypeBySendingOtpToMail
            (@Valid @RequestBody FmForgotPasswordOtpRequestDto requestDto) {

        FmForgotPasswordResponseDto fmForgotPasswordOtpResponseDto =
                fmForgotPasswordService.forgetPasswordForUserTypeBySendingOtpToMail(requestDto);

        return ResponseEntity.ok(fmForgotPasswordOtpResponseDto);
    }

//    validating OTP by comparing RedisStoredOTP = input otp
    @PostMapping("/validateForgotPasswordOTP")
    @Operation(
            summary = "Validate Forgot Password OTP",
            description = "Validates OTP stored in Redis against the OTP entered by user")
    public ResponseEntity<FmForgotPasswordResponseDto> validateForgotPasswordOtp
                (@RequestBody FmValidateForgotPasswordOtpRequestDto requestDto) {

        log.info("Validate OTP API called");
        FmForgotPasswordResponseDto fmForgotPasswordValidateResponseDto
                = fmForgotPasswordService.validateForgotPasswordOtp(requestDto);

        return ResponseEntity.ok(fmForgotPasswordValidateResponseDto);
    }

//     after validating the otp we are updating the password in and stores in encrypted format
    @PostMapping("/updateForgotPassword")
    @Operation(summary = "Update Forgot Password",
            description = "Updates user password after OTP validation")
    public ResponseEntity<FmForgotPasswordResponseDto> updateForgotPassword
            (@Valid @RequestBody FmUpdateForgotPasswordRequestDto requestDto) {

        FmForgotPasswordResponseDto fmUpdateForgotPasswordResponseDto
                = fmForgotPasswordService.updateForgotPassword(requestDto);

        log.info("UpdateForgotPassword API Called :");
        return ResponseEntity.ok(fmUpdateForgotPasswordResponseDto);
    }
}