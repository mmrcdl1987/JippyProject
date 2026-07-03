package com.jippy.foodandmart.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a secure 6-digit numeric OTP.
     *
     * Example:
     * 483921
     */
    public String generateOtp() {

        int otp = 100000 + SECURE_RANDOM.nextInt(900000);

        return String.valueOf(otp);
    }

}