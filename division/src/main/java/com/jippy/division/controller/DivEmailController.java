package com.jippy.division.controller;

import com.jippy.division.dto.DivSendOtpMailRequestDto;
import com.jippy.division.serviceImpl.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping("/api/div/email")
@RequiredArgsConstructor
@Slf4j
public class DivEmailController {

    private final EmailService emailService;

    @PostMapping("/sendOtp")
    public ResponseEntity<String> sendOtpMail(
            @RequestBody DivSendOtpMailRequestDto requestDto) {

        emailService.sendOtpMail(requestDto.getEmail(), requestDto.getOtp());

        log.info("OTP sent Successfully to email ID :"+requestDto.getEmail());

        return ResponseEntity.ok("OTP Mail Sent Successfully");
    }
}