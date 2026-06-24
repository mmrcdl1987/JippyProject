package com.jippy.foodandmart.service;


import com.jippy.foodandmart.dto.*;

public interface FmForgotPasswordService {

    FmForgotPasswordResponseDto forgetPasswordForUserTypeBySendingOtpToMail(
            FmForgotPasswordOtpRequestDto requestDto);

    FmForgotPasswordResponseDto validateForgotPasswordOtp(
            FmValidateForgotPasswordOtpRequestDto requestDto);

    FmForgotPasswordResponseDto updateForgotPassword(
            FmUpdateForgotPasswordRequestDto requestDto);
}
