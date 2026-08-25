package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.SchedulerSummaryDto;

import java.io.File;
import java.util.List;

/**
 * Common Email Service interface.
 *
 * Business layer should only depend on this interface.
 * Actual implementation can be Gmail, AWS SES, SendGrid etc.
 */
public interface EmailService {

    /**
     * Send OTP email.
     *
     * @param toEmail Recipient email
     * @param otp Generated OTP
     */
    void sendOtpEmail(String toEmail, String otp);
//
//    /**
//     * Send Welcome Email after successful registration.
//     *
//     * @param toEmail Merchant Email
//     * @param merchantName Merchant Name
//     */
//    void sendWelcomeEmail(String toEmail, String merchantName);

//    /**
//     * Send Forgot Password OTP.
//     *
//     * @param toEmail Merchant Email
//     * @param otp OTP
//     */
//    void sendForgotPasswordOtp(String toEmail, String otp);

    void sendMissingProductsEmail(
            SchedulerSummaryDto summary,
            File csvFile);


    void sendMerchantRegistrationEmail(
            String merchantEmail,
            String merchantName
    );

    void sendMerchantApprovedEmail(
            String merchantEmail,
            String merchantName
    );

    void sendOutletRegistrationEmail(
            String outletEmail,
            String outletName,
            String merchantName
    );

    void sendOutletOnlineEmail(
            String outletEmail,
            String outletName,
            String merchantName
    );

}