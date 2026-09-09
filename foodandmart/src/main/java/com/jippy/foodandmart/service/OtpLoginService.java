package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.AuthResponseDto;
import com.jippy.foodandmart.dto.SendLoginOtpRequestDto;
import com.jippy.foodandmart.dto.SendOtpResponseDto;
import com.jippy.foodandmart.dto.VerifyLoginOtpRequestDto;

public interface OtpLoginService {

    SendOtpResponseDto sendLoginOtp(
            SendLoginOtpRequestDto requestDto
    );

    SendOtpResponseDto resendLoginOtp(
            SendLoginOtpRequestDto requestDto
    );

    AuthResponseDto verifyLoginOtp(
            VerifyLoginOtpRequestDto requestDto
    );
}