package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.*;

public interface OtpService {

    ApiResponseDto sendOtp(SendOtpRequestDto request);

    JwtResponseDto verifyOtp(VerifyOtpRequestDto request);

    ApiResponseDto resendOtp(SendOtpRequestDto request);
}