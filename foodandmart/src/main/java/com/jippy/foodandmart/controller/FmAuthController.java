
package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.service.IFmOtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fm/otp")
@RequiredArgsConstructor
@Slf4j
public class FmAuthController {

    private final IFmOtpService otpService;

    /**
     * Send Signup OTP
     */
    @PostMapping("/send-signup-otp")
    public ResponseEntity<FmResponseDto> sendSignupOtp(
            @Valid @RequestBody FmSendOtpRequestDto request) {

        otpService.sendSignupOtp(request);

        return ResponseEntity.ok(
                new FmResponseDto("SUCCESS", "OTP sent successfully.")
        );
    }

    /**
     * Verify Signup OTP
     */
    @PostMapping("/verify-signup-otp")
    public ResponseEntity<FmResponseDto> verifySignupOtp(
            @Valid @RequestBody FmVerifyOtpRequestDto request) {

        return ResponseEntity.ok(
                otpService.verifySignupOtp(request)
        );
    }

    /**
     * Send Create Outlet OTP
     */
    @PostMapping("/send-create-outlet-otp")
    public ResponseEntity<FmResponseDto> sendCreateOutletOtp(
            @Valid @RequestBody FmCreateOutletOtpRequestDto request) {

        otpService.sendCreateOutletOtp(request);

        return ResponseEntity.ok(
                new FmResponseDto("SUCCESS", "OTP sent successfully.")
        );
    }

    /**
     * Verify Create Outlet OTP
     */
    @PostMapping("/verify-create-outlet-otp")
    public ResponseEntity<FmResponseDto> verifyCreateOutletOtp(
            @Valid @RequestBody FmVerifyOtpRequestDto request) {

        return ResponseEntity.ok(
                otpService.verifyCreateOutletOtp(request)
        );
    }

    @PostMapping("/resend-signup-otp")
    public ResponseEntity<FmResponseDto> resendSignupOtp(
            @Valid @RequestBody FmSendOtpRequestDto request) {

        otpService.resendSignupOtp(request);

        return ResponseEntity.ok(
                new FmResponseDto(
                        "SUCCESS",
                        "OTP resent successfully."
                )
        );
    }

    @PostMapping("/resend-create-outlet-otp")
    public ResponseEntity<FmResponseDto> resendCreateOutletOtp(
            @Valid @RequestBody FmCreateOutletOtpRequestDto request) {

        otpService.resendCreateOutletOtp(request);

        return ResponseEntity.ok(
                new FmResponseDto(
                        "SUCCESS",
                        "OTP resent successfully."
                )
        );
    }
}

//
///**
//     * Merchant Signup
//     */
//
//    @PostMapping("/signup")
//    public ResponseEntity<FmResponseDto> signupMerchant(
//            @RequestHeader("Signup-Token") String signupToken,
//            @Valid @RequestBody FmMerchantRequestDTO request) {
//
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(authService.signupMerchant(signupToken, request));
//    }
///**
//     * Send Forgot Password OTP
//     */
//
//    @PostMapping("/send-forgot-password-otp")
//    public ResponseEntity<FmResponseDto> sendForgotPasswordOtp(
//            @Valid @RequestBody FmForgotPasswordRequestDto request) {
//
//        otpService.sendForgotPasswordOtp(request);
//
//        return ResponseEntity.ok(
//                new FmResponseDto(
//                        "SUCCESS",
//                        "OTP sent successfully."
//                )
//        );
//    }
//
//
///**
//     * Verify Forgot Password OTP
//     */
//
//    @PostMapping("/verify-forgot-password-otp")
//    public ResponseEntity<FmResponseDto> verifyForgotPasswordOtp(
//            @Valid @RequestBody FmVerifyOtpRequestDto request) {
//
//        return ResponseEntity.ok(
//                otpService.verifyForgotPasswordOtp(request)
//        );
//    }
//
//
///**
//     * Reset Password
//     */
//
//    @PostMapping("/reset-password")
//    public ResponseEntity<FmResponseDto> resetPassword(
//            @Valid @RequestBody FmResetPasswordRequestDto request) {
//
//        return ResponseEntity.ok(
//                authService.resetPassword(request)
//        );
//    }
//}
