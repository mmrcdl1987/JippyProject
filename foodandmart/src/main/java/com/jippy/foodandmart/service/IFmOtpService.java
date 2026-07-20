package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.*;

public interface IFmOtpService {

    void sendSignupOtp(FmSendOtpRequestDto request);

    FmResponseDto verifySignupOtp(
            FmVerifyOtpRequestDto request
    );

    void sendCreateOutletOtp(
            FmCreateOutletOtpRequestDto request
    );

    FmResponseDto verifyCreateOutletOtp(
            FmVerifyOtpRequestDto request
    );
//
//    void sendForgotPasswordOtp(
//            FmForgotPasswordRequestDto request
//    );
//
//    FmResponseDto verifyForgotPasswordOtp(
//            FmVerifyOtpRequestDto request
//    );

    void resendSignupOtp(FmSendOtpRequestDto request);

    void resendCreateOutletOtp(FmCreateOutletOtpRequestDto request);
}