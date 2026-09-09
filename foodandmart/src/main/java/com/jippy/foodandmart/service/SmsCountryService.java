package com.jippy.foodandmart.service;

public interface SmsCountryService {

    String sendOtp(
            String mobileNumber,
            String otp
    );
}