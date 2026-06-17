package com.jippy.customerandorder.iservice;

public interface SmsCountryService {

    String sendOtp(
            String mobileNumber,
            String otp);
}