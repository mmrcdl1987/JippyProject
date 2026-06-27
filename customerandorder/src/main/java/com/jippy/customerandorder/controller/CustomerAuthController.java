package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.ApiResponseDto;
import com.jippy.customerandorder.dto.JwtResponseDto;
import com.jippy.customerandorder.dto.SendOtpRequestDto;
import com.jippy.customerandorder.dto.ValidateTokenResponseDto;
import com.jippy.customerandorder.dto.VerifyOtpRequestDto;
import com.jippy.customerandorder.iservice.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/co/auth")
@RequiredArgsConstructor
@Slf4j
public class CustomerAuthController {

    private final OtpService otpService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponseDto> sendOtp(@Valid @RequestBody SendOtpRequestDto request) {

        log.info("API | CUSTOMER_AUTH | SEND_OTP | START | mobile={}", request.getMobileNumber());
        try {
            ApiResponseDto response = otpService.sendOtp(request);
            log.info("API | CUSTOMER_AUTH | SEND_OTP | SUCCESS | mobile={} | message={}", request.getMobileNumber(), response.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("API | CUSTOMER_AUTH | SEND_OTP | ERROR | mobile={} | error={}", request.getMobileNumber(), ex.getMessage(), ex);
            throw ex;
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<JwtResponseDto> verifyOtp(@Valid @RequestBody VerifyOtpRequestDto request) {

        log.info("API | CUSTOMER_AUTH | VERIFY_OTP | START | mobile={}", request.getMobileNumber());
        try {
            JwtResponseDto response = otpService.verifyOtp(request);
            log.info("API | CUSTOMER_AUTH | VERIFY_OTP | SUCCESS | mobile={} | customerId={}", request.getMobileNumber(), response.getCustomerId());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("API | CUSTOMER_AUTH | VERIFY_OTP | ERROR | mobile={} | error={}", request.getMobileNumber(), ex.getMessage(), ex);
            throw ex;
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponseDto> resendOtp(@Valid @RequestBody SendOtpRequestDto request) {

        log.info("API | CUSTOMER_AUTH | RESEND_OTP | START | mobile={}", request.getMobileNumber());
        try {
            ApiResponseDto response = otpService.resendOtp(request);
            log.info("API | CUSTOMER_AUTH | RESEND_OTP | SUCCESS | mobile={} | message={}", request.getMobileNumber(), response.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("API | CUSTOMER_AUTH | RESEND_OTP | ERROR | mobile={} | error={}", request.getMobileNumber(), ex.getMessage(), ex);
            throw ex;
        }
    }

   /* @GetMapping("/validate-token")
    public ResponseEntity<ValidateTokenResponseDto> validateToken() {

        log.info("API | CUSTOMER_AUTH | VALIDATE_TOKEN | START");
        ValidateTokenResponseDto response = new ValidateTokenResponseDto();
        response.setValid(true);
        log.info("API | CUSTOMER_AUTH | VALIDATE_TOKEN | SUCCESS | valid={}", response.isValid());
        return ResponseEntity.ok(response);
    }*/
}