package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.SchedulerSummaryDto;
import com.jippy.foodandmart.exception.EmailSendingException;
import com.jippy.foodandmart.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
@RequiredArgsConstructor
@Slf4j
public class SesEmailService implements EmailService {

    // private final SesClient sesClient;
    private final JavaMailSender mailSender;

    @Value("${product-content.admin-email}")
    private String adminEmail;


    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        sendEmail(toEmail, FmAppConstants.FROM_EMAIL_NAME + " - Email Verification OTP", buildOtpTemplate(otp));
    }

    @Override
    public void sendMissingProductsEmail(SchedulerSummaryDto summary, File csvFile) {

        if (summary == null) {

            log.warn("Scheduler summary is null. Skipping email notification.");

            return;
        }

        log.info("Preparing Product Content Scheduler Report Email.");

        sendEmail(adminEmail, "Jippy - Product Content Scheduler Report", buildSchedulerSummaryTemplate(summary), csvFile);

        log.info("Product Content Scheduler Report Email sent successfully.");
    }

    @Override
    public void sendMerchantRegistrationEmail(String merchantEmail, String merchantName) {

        log.info("MERCHANT_REGISTRATION_EMAIL_START | email={}, merchantName={}", merchantEmail, merchantName);

        String subject = "Welcome to Jippy Mart - Merchant Registration Successful";

        String htmlBody = buildMerchantRegistrationTemplate(merchantName, merchantEmail);

        sendEmail(merchantEmail, subject, htmlBody);

        log.info("MERCHANT_REGISTRATION_EMAIL_SUCCESS | email={}", merchantEmail);
    }

    @Override
    public void sendMerchantApprovedEmail(String merchantEmail, String merchantName) {

        log.info("MERCHANT_APPROVED_EMAIL_START | email={}, merchantName={}", merchantEmail, merchantName);

        String subject = "Your Jippy Mart Merchant Account is Approved - Add Your Outlet";

        String htmlBody = buildMerchantApprovedTemplate(merchantName);

        sendEmail(merchantEmail, subject, htmlBody);

        log.info("MERCHANT_APPROVED_EMAIL_SUCCESS | email={}", merchantEmail);
    }

    @Override
    public void sendOutletRegistrationEmail(String outletEmail, String outletName, String merchantName) {

        log.info("OUTLET_REGISTRATION_EMAIL_START | email={}, outletName={}, merchantName={}", outletEmail, outletName, merchantName);

        String subject = "Welcome to Jippy Mart - Outlet Registration Successful";

        String htmlBody = buildOutletRegistrationTemplate(outletName, merchantName, outletEmail);

        sendEmail(outletEmail, subject, htmlBody);

        log.info("OUTLET_REGISTRATION_EMAIL_SUCCESS | email={}, outletName={}", outletEmail, outletName);
    }

    @Override
    public void sendOutletOnlineEmail(String outletEmail, String outletName, String merchantName) {

        log.info("OUTLET_ONLINE_EMAIL_START | email={}, outletName={}, merchantName={}", outletEmail, outletName, merchantName);

        String subject = "Congratulations! Your Outlet is Now Online - Jippy Mart";

        // Current date and time when outlet becomes ONLINE
        String goLiveDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        String htmlBody = buildOutletOnlineTemplate(outletName, merchantName, outletEmail, goLiveDate);

        sendEmail(outletEmail, subject, htmlBody);

        log.info("OUTLET_ONLINE_EMAIL_SUCCESS | email={}, outletName={}, goLiveDate={}", outletEmail, outletName, goLiveDate);
    }

    private String buildOutletRegistrationTemplate(String outletName, String merchantName, String outletEmail) {

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                
                    <title>Outlet Registration - Jippy Food Delivery</title>
                </head>
                
                <body style="
                    margin:0;
                    padding:0;
                    background:#F4F6F8;
                    font-family:Arial,Helvetica,sans-serif;
                    color:#222222;
                ">
                
                <table width="100%%"
                       cellpadding="0"
                       cellspacing="0"
                       style="background:#F4F6F8;">
                
                    <tr>
                        <td align="center" style="padding:25px 10px;">
                
                            <!-- MAIN CONTAINER -->
                
                            <table width="720"
                                   cellpadding="0"
                                   cellspacing="0"
                                   style="
                                       width:100%%;
                                       max-width:720px;
                                       background:#FFFFFF;
                                       border-radius:14px;
                                       overflow:hidden;
                                       border:1px solid #E5E5E5;
                                   ">
                
                                <!-- ================= HEADER ================= -->
                
                                <tr>
                                    <td style="
                                        padding:22px 30px;
                                        border-bottom:1px solid #EEEEEE;
                                    ">
                
                                        <table width="100%%">
                
                                            <tr>
                
                                                <td>
                
                                                    <img
                                                        src="https://s3-mmrcdl1987.s3.ap-south-1.amazonaws.com/unnamed.png"
                                                        width="170"
                                                        alt="Jippy Food Delivery"
                                                        style="
                                                            display:block;
                                                            border:0;
                                                        "
                                                    >
                
                                                </td>
                
                                                <td align="right"
                                                    style="
                                                        font-size:13px;
                                                        color:#555555;
                                                    ">
                
                                                    ✉ support@jippymart.in
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                                </tr>
                
                
                                <!-- ================= HERO ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:45px 35px;
                                        background:#FFF6EE;
                                    ">
                
                                        <table width="100%%">
                
                                            <tr>
                
                                                <td width="55%%"
                                                    valign="middle">
                
                                                    <div style="
                                                        font-size:36px;
                                                        line-height:45px;
                                                        font-weight:bold;
                                                        color:#222222;
                                                    ">
                
                                                        Welcome to
                
                                                    </div>
                
                                                    <div style="
                                                        font-size:42px;
                                                        line-height:50px;
                                                        font-weight:bold;
                                                        color:#F36F21;
                                                    ">
                
                                                        Jippy Food Delivery!
                
                                                    </div>
                
                                                    <p style="
                                                        margin:18px 0 0;
                                                        font-size:20px;
                                                        line-height:30px;
                                                        font-weight:bold;
                                                    ">
                
                                                        Your outlet registration
                                                        was successful.
                
                                                    </p>
                
                                                    <div style="
                                                        width:65px;
                                                        height:5px;
                                                        background:#F36F21;
                                                        margin:18px 0;
                                                        border-radius:5px;
                                                    "></div>
                
                                                </td>
                
                
                                                <td width="45%%"
                                                    align="center">
                
                                                    <div style="
                                                        font-size:90px;
                                                    ">
                                                        🏪
                                                    </div>
                
                                                    <div style="
                                                        display:inline-block;
                                                        padding:9px 18px;
                                                        background:#2E9B50;
                                                        border-radius:8px;
                                                        color:#FFFFFF;
                                                        font-size:14px;
                                                        font-weight:bold;
                                                    ">
                
                                                        ✓ REGISTRATION RECEIVED
                
                                                    </div>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= GREETING ================= -->
                
                                <tr>
                
                                    <td style="padding:35px 35px 15px;">
                
                                        <h2 style="
                                            margin:0 0 15px;
                                            color:#F36F21;
                                            font-size:27px;
                                        ">
                
                                            Hi %s,
                
                                        </h2>
                
                                        <p style="
                                            margin:0;
                                            font-size:17px;
                                            line-height:29px;
                                            color:#444444;
                                        ">
                
                                            Thank you for registering your outlet
                                            with <b>Jippy Food Delivery</b>.
                
                                        </p>
                
                                        <p style="
                                            margin:12px 0 0;
                                            font-size:17px;
                                            line-height:29px;
                                            color:#444444;
                                        ">
                
                                            Your outlet details have been
                                            successfully submitted. Our team will
                                            complete the required verification
                                            before your outlet goes online.
                
                                        </p>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= OUTLET DETAILS ================= -->
                
                                <tr>
                
                                    <td style="padding:20px 35px 30px;">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   border:1px solid #E5E5E5;
                                                   border-radius:10px;
                                                   overflow:hidden;
                                               ">
                
                                            <tr>
                
                                                <td style="
                                                    padding:20px;
                                                    background:#FFF8F2;
                                                    color:#F36F21;
                                                    font-size:21px;
                                                    font-weight:bold;
                                                ">
                
                                                    🏪 &nbsp; Outlet Details
                
                                                </td>
                
                                            </tr>
                
                
                                            <tr>
                
                                                <td style="padding:20px;">
                
                                                    <table width="100%%">
                
                                                        <tr>
                
                                                            <td width="42%%"
                                                                style="
                                                                    padding:9px 0;
                                                                    color:#666666;
                                                                    font-weight:bold;
                                                                ">
                
                                                                Outlet Name
                
                                                            </td>
                
                                                            <td style="
                                                                padding:9px 0;
                                                                color:#222222;
                                                            ">
                
                                                                %s
                
                                                            </td>
                
                                                        </tr>
                
                
                                                        <tr>
                
                                                            <td style="
                                                                padding:9px 0;
                                                                color:#666666;
                                                                font-weight:bold;
                                                            ">
                
                                                                Merchant Name
                
                                                            </td>
                
                                                            <td style="
                                                                padding:9px 0;
                                                                color:#222222;
                                                            ">
                
                                                                %s
                
                                                            </td>
                
                                                        </tr>
                
                
                                                        <tr>
                
                                                            <td style="
                                                                padding:9px 0;
                                                                color:#666666;
                                                                font-weight:bold;
                                                            ">
                
                                                                Outlet Email
                
                                                            </td>
                
                                                            <td style="
                                                                padding:9px 0;
                                                                color:#222222;
                                                            ">
                
                                                                %s
                
                                                            </td>
                
                                                        </tr>
                
                
                                                        <tr>
                
                                                            <td style="
                                                                padding:9px 0;
                                                                color:#666666;
                                                                font-weight:bold;
                                                            ">
                
                                                                Status
                
                                                            </td>
                
                                                            <td style="padding:9px 0;">
                
                                                                <span style="
                                                                    display:inline-block;
                                                                    padding:6px 12px;
                                                                    background:#FFF0C2;
                                                                    color:#8A5A00;
                                                                    border-radius:5px;
                                                                    font-size:13px;
                                                                    font-weight:bold;
                                                                ">
                
                                                                    PENDING VERIFICATION
                
                                                                </span>
                
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= WHAT'S NEXT ================= -->
                
                                <tr>
                
                                    <td style="padding:0 35px 30px;">
                
                                        <table width="100%%"
                                               style="
                                                   background:#F5FFF7;
                                                   border:1px solid #D6EEDB;
                                                   border-radius:12px;
                                               ">
                
                                            <tr>
                
                                                <td style="padding:25px;">
                
                                                    <div style="
                                                        font-size:22px;
                                                        font-weight:bold;
                                                        color:#2E9B50;
                                                        margin-bottom:15px;
                                                    ">
                
                                                        📋 &nbsp; What's Next?
                
                                                    </div>
                
                
                                                    <p style="
                                                        margin:10px 0;
                                                        font-size:16px;
                                                        line-height:27px;
                                                    ">
                
                                                        <span style="
                                                            color:#2E9B50;
                                                            font-weight:bold;
                                                        ">✓</span>
                
                                                        &nbsp; Our team will review
                                                        your outlet details.
                
                                                    </p>
                
                
                                                    <p style="
                                                        margin:10px 0;
                                                        font-size:16px;
                                                        line-height:27px;
                                                    ">
                
                                                        <span style="
                                                            color:#2E9B50;
                                                            font-weight:bold;
                                                        ">✓</span>
                
                                                        &nbsp; Complete any remaining
                                                        verification.
                
                                                    </p>
                
                
                                                    <p style="
                                                        margin:10px 0;
                                                        font-size:16px;
                                                        line-height:27px;
                                                    ">
                
                                                        <span style="
                                                            color:#2E9B50;
                                                            font-weight:bold;
                                                        ">✓</span>
                
                                                        &nbsp; Add and manage your
                                                        menu/products.
                
                                                    </p>
                
                
                                                    <p style="
                                                        margin:10px 0;
                                                        font-size:16px;
                                                        line-height:27px;
                                                    ">
                
                                                        <span style="
                                                            color:#2E9B50;
                                                            font-weight:bold;
                                                        ">✓</span>
                
                                                        &nbsp; Once activated, your
                                                        outlet can go online.
                
                                                    </p>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= JIPPY PROMISE ================= -->
                
                                <tr>
                
                                    <td style="padding:0 35px 30px;">
                
                                        <table width="100%%"
                                               style="
                                                   background:#F2FFF4;
                                                   border:1px solid #CDE8D2;
                                                   border-radius:12px;
                                               ">
                
                                            <tr>
                
                                                <td style="padding:25px;">
                
                                                    <div style="
                                                        font-size:22px;
                                                        font-weight:bold;
                                                        color:#2E9B50;
                                                        margin-bottom:8px;
                                                    ">
                
                                                        🛡️ Jippy Promise
                
                                                    </div>
                
                                                    <div style="
                                                        font-size:18px;
                                                        font-weight:bold;
                                                    ">
                
                                                        No Commission.
                                                        No Deductions.
                
                                                    </div>
                
                                                    <p style="
                                                        margin:8px 0 0;
                                                        font-size:15px;
                                                        line-height:25px;
                                                        color:#555555;
                                                    ">
                
                                                        Jippy Food Delivery is not
                                                        collecting any commission.
                                                        Jippy Food Delivery settles
                                                        the merchant price agreed
                                                        with you, with no deductions.
                
                                                    </p>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= SUPPORT ================= -->
                
                                <tr>
                
                                    <td style="padding:0 35px 30px;">
                
                                        <table width="100%%"
                                               style="
                                                   background:#F1F7FF;
                                                   border:1px solid #C9DDF5;
                                                   border-radius:10px;
                                               ">
                
                                            <tr>
                
                                                <td style="
                                                    padding:22px;
                                                    text-align:center;
                                                ">
                
                                                    <div style="
                                                        font-size:22px;
                                                        font-weight:bold;
                                                        color:#1769AA;
                                                    ">
                
                                                        🎧 Need Help?
                
                                                    </div>
                
                                                    <p style="
                                                        margin:8px 0;
                                                        font-size:15px;
                                                        color:#555555;
                                                    ">
                
                                                        Our team is happy to help you.
                
                                                    </p>
                
                                                    <a href="mailto:support@jippymart.in"
                                                       style="
                                                           color:#1769AA;
                                                           font-weight:bold;
                                                           text-decoration:none;
                                                       ">
                
                                                        ✉ support@jippymart.in
                
                                                    </a>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= FOOTER ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:28px 20px;
                                        background:#1E2227;
                                        text-align:center;
                                        color:#FFFFFF;
                                    ">
                
                                        <div style="
                                            font-size:25px;
                                            font-weight:bold;
                                            margin-bottom:14px;
                                        ">
                
                                            🛍️
                
                                            <span style="color:#FFFFFF;">
                                                Jippy
                                            </span>
                
                                            <span style="color:#F36F21;">
                                                Food Delivery
                                            </span>
                
                                        </div>
                
                                        <p style="
                                            margin:5px 0;
                                            font-size:13px;
                                            color:#CCCCCC;
                                        ">
                
                                            © 2026 Jippy Technologies Pvt. Ltd.
                                            All Rights Reserved.
                
                                        </p>
                
                                        <p style="
                                            margin:10px 0 0;
                                            font-size:12px;
                                            color:#AAAAAA;
                                        ">
                
                                            Privacy Policy
                                            &nbsp; | &nbsp;
                                            Terms & Conditions
                                            &nbsp; | &nbsp;
                                            Support
                
                                        </p>
                
                                    </td>
                
                                </tr>
                
                            </table>
                
                        </td>
                    </tr>
                
                </table>
                
                </body>
                </html>
                """.formatted(outletName, outletName, merchantName, outletEmail);
    }

    private String buildOutletOnlineTemplate(String outletName, String merchantName, String outletEmail, String goLiveDate) {

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                
                    <title>Your Outlet is Now Online - Jippy Food Delivery</title>
                </head>
                
                <body style="
                    margin:0;
                    padding:0;
                    background:#F4F6F8;
                    font-family:Arial,Helvetica,sans-serif;
                    color:#20242A;
                ">
                
                <table width="100%%"
                       cellpadding="0"
                       cellspacing="0"
                       border="0"
                       style="background:#F4F6F8;">
                
                    <tr>
                        <td align="center"
                            style="padding:20px 10px;">
                
                            <!-- MAIN CONTAINER -->
                
                            <table width="760"
                                   cellpadding="0"
                                   cellspacing="0"
                                   border="0"
                                   style="
                                       width:100%%;
                                       max-width:760px;
                                       background:#FFFFFF;
                                       border:1px solid #DDDDDD;
                                       border-radius:8px;
                                       overflow:hidden;
                                   ">
                
                
                                <!-- ================================================= -->
                                <!-- HEADER -->
                                <!-- ================================================= -->
                
                                <tr>
                                    <td style="
                                        padding:18px 38px;
                                        background:#FFFFFF;
                                        border-bottom:1px solid #EEEEEE;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0">
                
                                            <tr>
                
                                                <!-- LOGO -->
                
                                                <td align="left">
                
                                                    <div style="
                                                        font-size:38px;
                                                        font-weight:bold;
                                                        line-height:42px;
                                                        color:#20242A;
                                                    ">
                
                                                        🛍️
                
                                                        <span style="
                                                            color:#20242A;
                                                        ">
                                                            Jippy
                                                        </span>
                
                                                        <span style="
                                                            color:#F36F21;
                                                        ">
                                                            Food Delivery
                                                        </span>
                
                                                    </div>
                
                                                </td>
                
                
                                                <!-- SUPPORT EMAIL -->
                
                                                <td align="right"
                                                    valign="middle"
                                                    style="
                                                        font-size:15px;
                                                        color:#222222;
                                                    ">
                
                                                    <span style="
                                                        color:#F36F21;
                                                        font-size:22px;
                                                    ">
                                                        ✉
                                                    </span>
                
                                                    &nbsp;
                
                                                    support@jippymart.in
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                                </tr>
                
                
                                <!-- ================================================= -->
                                <!-- HERO -->
                                <!-- ================================================= -->
                
                                <tr>
                                    <td style="
                                        background:#EAF4FF;
                                        padding:34px 38px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0">
                
                                            <tr>
                
                                                <!-- HERO TEXT -->
                
                                                <td width="55%%"
                                                    valign="middle">
                
                                                    <div style="
                                                        font-size:42px;
                                                        line-height:47px;
                                                        font-weight:700;
                                                        color:#20242A;
                                                    ">
                
                                                        Your Outlet is
                
                                                    </div>
                
                                                    <div style="
                                                        font-size:46px;
                                                        line-height:52px;
                                                        font-weight:700;
                                                        color:#F36F21;
                                                    ">
                
                                                        Now Online!
                
                                                    </div>
                
                
                                                    <div style="
                                                        width:62px;
                                                        height:4px;
                                                        background:#198F42;
                                                        margin:16px 0;
                                                    "></div>
                
                
                                                    <div style="
                                                        font-size:25px;
                                                        line-height:30px;
                                                        font-weight:bold;
                                                        color:#168A3E;
                                                    ">
                
                                                        Congratulations!
                
                                                    </div>
                
                
                                                    <p style="
                                                        margin:10px 0 0;
                                                        font-size:17px;
                                                        line-height:26px;
                                                        color:#222222;
                                                    ">
                
                                                        Your outlet is now live on
                                                        <b>Jippy Food Delivery.</b>
                
                                                    </p>
                
                
                                                    <p style="
                                                        margin:6px 0 0;
                                                        font-size:16px;
                                                        line-height:25px;
                                                        color:#333333;
                                                    ">
                
                                                        Customers can now discover
                                                        your outlet and place orders.
                
                                                    </p>
                
                                                </td>
                
                
                                                <!-- HERO IMAGE / ICON -->
                
                                                <td width="45%%"
                                                    align="center"
                                                    valign="middle">
                
                                                    <div style="
                                                        font-size:105px;
                                                        line-height:105px;
                                                    ">
                
                                                        🏪
                
                                                    </div>
                
                
                                                    <div style="
                                                        display:inline-block;
                                                        background:#159447;
                                                        color:#FFFFFF;
                                                        padding:9px 25px;
                                                        border-radius:8px;
                                                        font-size:21px;
                                                        font-weight:bold;
                                                    ">
                
                                                        ● LIVE
                
                                                    </div>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                                </tr>
                
                
                                <!-- ================================================= -->
                                <!-- GREETING -->
                                <!-- ================================================= -->
                
                                <tr>
                                    <td style="
                                        padding:20px 38px 5px;
                                    ">
                
                                        <div style="
                                            font-size:27px;
                                            font-weight:bold;
                                            color:#F36F21;
                                            margin-bottom:12px;
                                        ">
                
                                            Hi %s,
                
                                        </div>
                
                
                                        <p style="
                                            margin:0;
                                            font-size:17px;
                                            line-height:27px;
                                            color:#333333;
                                        ">
                
                                            Great news! Your outlet has been
                                            successfully activated and is now
                
                                            <b style="color:#168A3E;">
                                                ONLINE
                                            </b>
                
                                            on Jippy Food Delivery.
                
                                        </p>
                
                
                                        <p style="
                                            margin:10px 0 0;
                                            font-size:17px;
                                            line-height:27px;
                                            color:#333333;
                                        ">
                
                                            Start managing your orders and grow
                                            your business with Jippy Food Delivery.
                
                                            🤝
                
                                        </p>
                
                                    </td>
                                </tr>
                
                
                                <!-- ================================================= -->
                                <!-- OUTLET DETAILS -->
                                <!-- ================================================= -->
                
                                <tr>
                                    <td style="
                                        padding:15px 38px 8px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   border:1px solid #DCDCDC;
                                                   border-radius:12px;
                                                   background:#FFFFFF;
                                               ">
                
                                            <tr>
                
                                                <td style="
                                                    padding:20px;
                                                ">
                
                                                    <!-- TITLE -->
                
                                                    <div style="
                                                        font-size:21px;
                                                        font-weight:bold;
                                                        color:#F36F21;
                                                        margin-bottom:12px;
                                                    ">
                
                                                        🏪
                                                        &nbsp;
                                                        OUTLET DETAILS
                
                                                    </div>
                
                
                                                    <table width="100%%"
                                                           cellpadding="0"
                                                           cellspacing="0">
                
                                                        <tr>
                
                                                            <!-- LEFT -->
                
                                                            <td width="50%%"
                                                                valign="top"
                                                                style="
                                                                    padding-right:15px;
                                                                ">
                
                
                                                                <p style="
                                                                    margin:8px 0;
                                                                    font-size:15px;
                                                                ">
                
                                                                    🏪
                                                                    &nbsp;
                
                                                                    <b>
                                                                        Outlet Name
                                                                    </b>
                
                                                                    &nbsp;&nbsp;:
                
                                                                    %s
                
                                                                </p>
                
                
                                                                <p style="
                                                                    margin:8px 0;
                                                                    font-size:15px;
                                                                ">
                
                                                                    👤
                                                                    &nbsp;
                
                                                                    <b>
                                                                        Merchant Name
                                                                    </b>
                
                                                                    &nbsp;:
                
                                                                    %s
                
                                                                </p>
                
                
                                                                <p style="
                                                                    margin:8px 0;
                                                                    font-size:15px;
                                                                ">
                
                                                                    ✉
                                                                    &nbsp;
                
                                                                    <b>
                                                                        Outlet Email
                                                                    </b>
                
                                                                    &nbsp;&nbsp;:
                
                                                                    %s
                
                                                                </p>
                
                                                            </td>
                
                
                                                            <!-- RIGHT -->
                
                                                            <td width="50%%"
                                                                valign="top"
                                                                style="
                                                                    padding-left:15px;
                                                                ">
                
                
                                                                <p style="
                                                                    margin:8px 0;
                                                                    font-size:15px;
                                                                ">
                
                                                                    🟢
                                                                    &nbsp;
                
                                                                    <b>
                                                                        Status
                                                                    </b>
                
                                                                    &nbsp;&nbsp;:
                
                                                                    <span style="
                                                                        color:#168A3E;
                                                                        font-weight:bold;
                                                                    ">
                                                                        ONLINE
                                                                    </span>
                
                                                                </p>
                
                
                                                                <p style="
                                                                    margin:8px 0;
                                                                    font-size:15px;
                                                                ">
                
                                                                    📅
                                                                    &nbsp;
                
                                                                    <b>
                                                                        Go Live Date
                                                                    </b>
                
                                                                    &nbsp;:
                
                                                                    %s
                
                                                                </p>
                
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                                </tr>
                
                
                                <!-- ================================================= -->
                                <!-- NEXT STEPS -->
                                <!-- ================================================= -->
                
                                <tr>
                                    <td style="
                                        padding:8px 38px 15px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   border:1px solid #B7DDC5;
                                                   border-radius:12px;
                                                   background:#F8FCF9;
                                               ">
                
                                            <tr>
                
                                                <td style="
                                                    padding:18px 15px 20px;
                                                ">
                
                
                                                    <!-- TITLE -->
                
                                                    <div style="
                                                        text-align:center;
                                                        font-size:22px;
                                                        font-weight:bold;
                                                        color:#168A3E;
                                                        margin-bottom:18px;
                                                    ">
                
                                                        🎯
                                                        &nbsp;
                                                        NEXT STEPS
                
                                                    </div>
                
                
                                                    <table width="100%%"
                                                           cellpadding="0"
                                                           cellspacing="0">
                
                                                        <tr>
                
                
                                                            <!-- STEP 1 -->
                
                                                            <td width="25%%"
                                                                align="center"
                                                                valign="top"
                                                                style="
                                                                    padding:5px 12px;
                                                                    border-right:1px solid #B8C5BE;
                                                                ">
                
                                                                <div style="
                                                                    font-size:40px;
                                                                    line-height:45px;
                                                                ">
                                                                    📋
                                                                </div>
                
                
                                                                <div style="
                                                                    font-size:16px;
                                                                    font-weight:bold;
                                                                    color:#168A3E;
                                                                    margin-top:7px;
                                                                ">
                
                                                                    Manage Menu
                
                                                                </div>
                
                
                                                                <div style="
                                                                    font-size:13px;
                                                                    line-height:19px;
                                                                    color:#333333;
                                                                    margin-top:7px;
                                                                ">
                
                                                                    Keep your menu
                                                                    and stock updated.
                
                                                                </div>
                
                                                            </td>
                
                
                                                            <!-- STEP 2 -->
                
                                                            <td width="25%%"
                                                                align="center"
                                                                valign="top"
                                                                style="
                                                                    padding:5px 12px;
                                                                    border-right:1px solid #B8C5BE;
                                                                ">
                
                                                                <div style="
                                                                    font-size:40px;
                                                                    line-height:45px;
                                                                ">
                                                                    📦
                                                                </div>
                
                
                                                                <div style="
                                                                    font-size:16px;
                                                                    font-weight:bold;
                                                                    color:#168A3E;
                                                                    margin-top:7px;
                                                                ">
                
                                                                    Accept Orders
                
                                                                </div>
                
                
                                                                <div style="
                                                                    font-size:13px;
                                                                    line-height:19px;
                                                                    color:#333333;
                                                                    margin-top:7px;
                                                                ">
                
                                                                    Accept and fulfill
                                                                    customer orders.
                
                                                                </div>
                
                                                            </td>
                
                
                                                            <!-- STEP 3 -->
                
                                                            <td width="25%%"
                                                                align="center"
                                                                valign="top"
                                                                style="
                                                                    padding:5px 12px;
                                                                    border-right:1px solid #B8C5BE;
                                                                ">
                
                                                                <div style="
                                                                    font-size:40px;
                                                                    line-height:45px;
                                                                ">
                                                                    ⭐
                                                                </div>
                
                
                                                                <div style="
                                                                    font-size:16px;
                                                                    font-weight:bold;
                                                                    color:#168A3E;
                                                                    margin-top:7px;
                                                                ">
                
                                                                    Quality Service
                
                                                                </div>
                
                
                                                                <div style="
                                                                    font-size:13px;
                                                                    line-height:19px;
                                                                    color:#333333;
                                                                    margin-top:7px;
                                                                ">
                
                                                                    Maintain quality
                                                                    and good service.
                
                                                                </div>
                
                                                            </td>
                
                
                                                            <!-- STEP 4 -->
                
                                                            <td width="25%%"
                                                                align="center"
                                                                valign="top"
                                                                style="
                                                                    padding:5px 12px;
                                                                ">
                
                                                                <div style="
                                                                    font-size:40px;
                                                                    line-height:45px;
                                                                ">
                                                                    📈
                                                                </div>
                
                
                                                                <div style="
                                                                    font-size:16px;
                                                                    font-weight:bold;
                                                                    color:#168A3E;
                                                                    margin-top:7px;
                                                                ">
                
                                                                    Grow Business
                
                                                                </div>
                
                
                                                                <div style="
                                                                    font-size:13px;
                                                                    line-height:19px;
                                                                    color:#333333;
                                                                    margin-top:7px;
                                                                ">
                
                                                                    Grow your business
                                                                    with Jippy Food Delivery.
                
                                                                </div>
                
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                                </tr>
                
                
                                <!-- ================================================= -->
                                <!-- JIPPY POLICIES -->
                                <!-- ================================================= -->
                
                                <tr>
                                    <td style="
                                        padding:0 38px 15px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   border:1px solid #F2A15E;
                                                   border-radius:12px;
                                                   background:#FFFDFC;
                                               ">
                
                
                                            <!-- POLICY HEADER -->
                
                                            <tr>
                
                                                <td style="
                                                    padding:15px 20px;
                                                    text-align:center;
                                                    background:#FFFFFF;
                                                ">
                
                                                    <div style="
                                                        font-size:22px;
                                                        font-weight:bold;
                                                        color:#F36F21;
                                                    ">
                
                                                        🛡️
                                                        &nbsp;
                                                        JIPPY POLICIES
                
                                                    </div>
                
                                                </td>
                
                                            </tr>
                
                
                                            <!-- POLICY CONTENT -->
                
                                            <tr>
                
                                                <td style="
                                                    padding:8px 15px 15px;
                                                ">
                
                                                    <table width="100%%"
                                                           cellpadding="0"
                                                           cellspacing="0">
                
                
                                                        <!-- ROW 1 -->
                
                                                        <tr>
                
                
                                                            <!-- POLICY 1 -->
                
                                                            <td width="33%%"
                                                                valign="top"
                                                                style="
                                                                    padding:12px;
                                                                    border-right:1px dashed #E6C9AE;
                                                                ">
                
                                                                <div style="
                                                                    font-size:32px;
                                                                    margin-bottom:7px;
                                                                ">
                                                                    💰
                                                                </div>
                
                
                                                                <div style="
                                                                    font-size:15px;
                                                                    font-weight:bold;
                                                                    color:#20242A;
                                                                ">
                
                                                                    No Commission.<br>
                                                                    No Deductions.
                
                                                                </div>
                
                
                                                                <p style="
                                                                    margin:7px 0 0;
                                                                    font-size:13px;
                                                                    line-height:19px;
                                                                    color:#333333;
                                                                ">
                
                                                                    Jippy is not
                                                                    collecting any
                                                                    commission.
                
                                                                    Jippy settles
                                                                    the merchant
                                                                    price agreed
                                                                    with you,
                                                                    with no
                                                                    deductions.
                
                                                                </p>
                
                                                            </td>
                
                
                                                            <!-- POLICY 2 -->
                
                                                            <td width="33%%"
                                                                valign="top"
                                                                style="
                                                                    padding:12px;
                                                                    border-right:1px dashed #E6C9AE;
                                                                ">
                
                                                                <div style="
                                                                    font-size:32px;
                                                                    margin-bottom:7px;
                                                                ">
                                                                    🕐
                                                                </div>
                
                
                                                                <div style="
                                                                    font-size:15px;
                                                                    font-weight:bold;
                                                                ">
                
                                                                    Don't Close Outlet<br>
                                                                    Before Timings.
                
                                                                </div>
                
                
                                                                <p style="
                                                                    margin:7px 0 0;
                                                                    font-size:13px;
                                                                    line-height:19px;
                                                                    color:#333333;
                                                                ">
                
                                                                    Please don't
                                                                    close your
                                                                    outlet before
                                                                    your configured
                                                                    outlet timings.
                
                                                                    Your orders may
                                                                    get impacted.
                
                                                                </p>
                
                                                            </td>
                
                
                                                            <!-- POLICY 3 -->
                
                                                            <td width="34%%"
                                                                valign="top"
                                                                style="
                                                                    padding:12px;
                                                                ">
                
                                                                <div style="
                                                                    font-size:32px;
                                                                    margin-bottom:7px;
                                                                ">
                                                                    🛡️
                                                                </div>
                
                
                                                                <div style="
                                                                    font-size:15px;
                                                                    font-weight:bold;
                                                                ">
                
                                                                    Maintain<br>
                                                                    Outlet Hygiene.
                
                                                                </div>
                
                
                                                                <p style="
                                                                    margin:7px 0 0;
                                                                    font-size:13px;
                                                                    line-height:19px;
                                                                    color:#333333;
                                                                ">
                
                                                                    Please maintain
                                                                    good outlet and
                                                                    food hygiene to
                                                                    help avoid food
                                                                    spoilage and
                                                                    quality issues.
                
                                                                </p>
                
                                                            </td>
                
                                                        </tr>
                
                
                                                        <!-- ROW 2 -->
                
                                                        <tr>
                
                
                                                            <!-- POLICY 4 -->
                
                                                            <td width="33%%"
                                                                valign="top"
                                                                style="
                                                                    padding:12px;
                                                                    border-top:1px dashed #E6C9AE;
                                                                    border-right:1px dashed #E6C9AE;
                                                                ">
                
                                                                <div style="
                                                                    font-size:32px;
                                                                    margin-bottom:7px;
                                                                ">
                                                                    📦
                                                                </div>
                
                
                                                                <div style="
                                                                    font-size:15px;
                                                                    font-weight:bold;
                                                                ">
                
                                                                    Try to Fulfill<br>
                                                                    All Orders.
                
                                                                </div>
                
                
                                                                <p style="
                                                                    margin:7px 0 0;
                                                                    font-size:13px;
                                                                    line-height:19px;
                                                                    color:#333333;
                                                                ">
                
                                                                    Please try to
                                                                    fulfill all
                                                                    orders you
                                                                    receive.
                
                                                                    Rejected orders
                                                                    may impact your
                                                                    outlet
                                                                    performance.
                
                                                                    Our AI analyzes
                                                                    outlet
                                                                    performance,
                                                                    and repeated
                                                                    rejections may
                                                                    reduce future
                                                                    orders.
                
                                                                </p>
                
                                                            </td>
                
                
                                                            <!-- POLICY 5 -->
                
                                                            <td width="33%%"
                                                                valign="top"
                                                                style="
                                                                    padding:12px;
                                                                    border-top:1px dashed #E6C9AE;
                                                                    border-right:1px dashed #E6C9AE;
                                                                ">
                
                                                                <div style="
                                                                    font-size:32px;
                                                                    margin-bottom:7px;
                                                                ">
                                                                    📦
                                                                </div>
                
                
                                                                <div style="
                                                                    font-size:15px;
                                                                    font-weight:bold;
                                                                ">
                
                                                                    Manage Stock<br>
                                                                    Properly.
                
                                                                </div>
                
                
                                                                <p style="
                                                                    margin:7px 0 0;
                                                                    font-size:13px;
                                                                    line-height:19px;
                                                                    color:#333333;
                                                                ">
                
                                                                    If you are
                                                                    facing any
                                                                    stock issues,
                                                                    please turn
                                                                    off that
                                                                    product until
                                                                    stock is
                                                                    available.
                
                                                                    Receiving an
                                                                    order and then
                                                                    cancelling it
                                                                    is not good for
                                                                    your outlet
                                                                    performance.
                
                                                                </p>
                
                                                            </td>
                
                
                                                            <!-- POLICY 6 -->
                
                                                            <td width="34%%"
                                                                valign="top"
                                                                style="
                                                                    padding:12px;
                                                                    border-top:1px dashed #E6C9AE;
                                                                ">
                
                                                                <div style="
                                                                    font-size:32px;
                                                                    margin-bottom:7px;
                                                                ">
                                                                    💳
                                                                </div>
                
                
                                                                <div style="
                                                                    font-size:15px;
                                                                    font-weight:bold;
                                                                ">
                
                                                                    Check Your<br>
                                                                    Settlements<br>
                                                                    Every Week.
                
                                                                </div>
                
                
                                                                <p style="
                                                                    margin:7px 0 0;
                                                                    font-size:13px;
                                                                    line-height:19px;
                                                                    color:#333333;
                                                                ">
                
                                                                    Please check
                                                                    your settlement
                                                                    details every
                                                                    week.
                
                                                                    If you have
                                                                    any questions
                                                                    or concerns
                                                                    about your
                                                                    settlement,
                                                                    please reach us
                                                                    at:
                
                                                                    <br>
                
                                                                    <b style="
                                                                        color:#F36F21;
                                                                    ">
                
                                                                        support@jippymart.in
                
                                                                    </b>
                
                                                                </p>
                
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                                </tr>
                
                
                                <!-- ================================================= -->
                                <!-- THANK YOU -->
                                <!-- ================================================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 38px 10px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   background:#FFF8E5;
                                                   border:1px solid #EEC94A;
                                                   border-radius:10px;
                                               ">
                
                                            <tr>
                
                                                <td width="18%%"
                                                    align="right"
                                                    style="
                                                        padding:12px 5px;
                                                        font-size:38px;
                                                    ">
                                                    🏆
                                                </td>
                
                
                                                <td width="64%%"
                                                    align="center"
                                                    style="
                                                        padding:12px 5px;
                                                    ">
                
                                                    <div style="
                                                        font-size:16px;
                                                        color:#704900;
                                                    ">
                
                                                        Thank you for being a
                                                        valuable Jippy Food Delivery
                                                        partner.
                
                                                    </div>
                
                
                                                    <div style="
                                                        font-size:18px;
                                                        font-weight:bold;
                                                        color:#F36F21;
                                                        margin-top:3px;
                                                    ">
                
                                                        Together, let's deliver the
                                                        best experience to our
                                                        customers!
                
                                                    </div>
                
                                                </td>
                
                
                                                <td width="18%%"
                                                    align="left"
                                                    style="
                                                        padding:12px 5px;
                                                        font-size:32px;
                                                    ">
                                                    🎉
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================================================= -->
                                <!-- SUPPORT -->
                                <!-- ================================================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:10px 38px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   background:#F4F8FC;
                                                   border:1px solid #BFD3E6;
                                                   border-radius:10px;
                                               ">
                
                                            <tr>
                
                                                <td width="25%%"
                                                    align="right"
                                                    style="
                                                        padding:10px;
                                                        font-size:40px;
                                                    ">
                                                    🎧
                                                </td>
                
                
                                                <td width="75%%"
                                                    style="
                                                        padding:10px;
                                                    ">
                
                                                    <div style="
                                                        font-size:17px;
                                                        font-weight:bold;
                                                        color:#20242A;
                                                    ">
                
                                                        Need help?
                
                                                        <span style="
                                                            color:#F36F21;
                                                        ">
                
                                                            support@jippymart.in
                
                                                        </span>
                
                                                    </div>
                
                
                                                    <div style="
                                                        margin-top:4px;
                                                        font-size:13px;
                                                        color:#555555;
                                                    ">
                
                                                        Our team is happy to help you.
                
                                                    </div>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================================================= -->
                                <!-- FOOTER -->
                                <!-- ================================================= -->
                
                                <tr>
                
                                    <td style="
                                        margin-top:10px;
                                        padding:20px 30px;
                                        background:#20262B;
                                        color:#FFFFFF;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0">
                
                                            <tr>
                
                                                <!-- FOOTER LOGO -->
                
                                                <td width="25%%"
                                                    valign="middle">
                
                                                    <div style="
                                                        font-size:25px;
                                                        font-weight:bold;
                                                    ">
                
                                                        🛍️
                
                                                        Jippy
                
                                                        <span style="
                                                            color:#F36F21;
                                                        ">
                
                                                            Food Delivery
                
                                                        </span>
                
                                                    </div>
                
                                                </td>
                
                
                                                <!-- COPYRIGHT -->
                
                                                <td width="35%%"
                                                    valign="middle"
                                                    style="
                                                        border-left:1px solid #AAAAAA;
                                                        padding-left:20px;
                                                        font-size:12px;
                                                        color:#DDDDDD;
                                                    ">
                
                                                    © 2026 Jippy Technologies Pvt. Ltd.
                
                                                    <br>
                
                                                    All Rights Reserved.
                
                                                </td>
                
                
                                                <!-- LINKS -->
                
                                                <td width="40%%"
                                                    align="right"
                                                    valign="middle"
                                                    style="
                                                        font-size:12px;
                                                        color:#FFFFFF;
                                                    ">
                
                                                    Privacy Policy
                
                                                    &nbsp;&nbsp;|&nbsp;&nbsp;
                
                                                    Terms & Conditions
                
                                                    &nbsp;&nbsp;|&nbsp;&nbsp;
                
                                                    Support
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                            </table>
                
                        </td>
                    </tr>
                
                </table>
                
                </body>
                </html>
                """.formatted(merchantName, outletName, merchantName, outletEmail, goLiveDate);
    }

    private String buildMerchantApprovedTemplate(String merchantName) {

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                
                    <title>Merchant Account Approved - Jippy Food Delivery</title>
                </head>
                
                <body style="
                    margin:0;
                    padding:0;
                    background:#F4F6F8;
                    font-family:Arial,Helvetica,sans-serif;
                    color:#222222;
                ">
                
                <table width="100%%"
                       cellpadding="0"
                       cellspacing="0"
                       border="0"
                       style="background:#F4F6F8;">
                
                    <tr>
                        <td align="center" style="padding:25px 10px;">
                
                            <!-- MAIN CONTAINER -->
                            <table width="720"
                                   cellpadding="0"
                                   cellspacing="0"
                                   border="0"
                                   style="
                                       width:100%%;
                                       max-width:720px;
                                       background:#FFFFFF;
                                       border-radius:14px;
                                       overflow:hidden;
                                       border:1px solid #E5E5E5;
                                   ">
                
                                <!-- ================= HEADER ================= -->
                
                                <tr>
                                    <td style="
                                        padding:22px 30px;
                                        background:#FFFFFF;
                                        border-bottom:1px solid #EEEEEE;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0">
                
                                            <tr>
                
                                                <td align="left">
                
                                                    <img
                                                        src="https://s3-mmrcdl1987.s3.ap-south-1.amazonaws.com/unnamed.png"
                                                        width="170"
                                                        alt="Jippy Food Delivery"
                                                        style="
                                                            display:block;
                                                            border:0;
                                                            max-width:170px;
                                                        "
                                                    >
                
                                                </td>
                
                                                <td align="right"
                                                    style="
                                                        font-size:13px;
                                                        color:#555555;
                                                    ">
                
                                                    ✉
                                                    support@jippymart.in
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                                </tr>
                
                
                                <!-- ================= HERO ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:45px 35px;
                                        background:#FFF6EE;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0">
                
                                            <tr>
                
                                                <td width="55%%"
                                                    valign="middle"
                                                    style="padding-right:15px;">
                
                                                    <div style="
                                                        font-size:36px;
                                                        line-height:44px;
                                                        font-weight:bold;
                                                        color:#F36F21;
                                                    ">
                
                                                        🎉 GREAT NEWS!
                
                                                    </div>
                
                                                    <div style="
                                                        margin-top:18px;
                                                        font-size:30px;
                                                        line-height:40px;
                                                        font-weight:bold;
                                                        color:#222222;
                                                    ">
                
                                                        Your Jippy Food Delivery
                                                        merchant account
                                                        has been
                
                                                        <span style="
                                                            color:#2E9B50;
                                                        ">
                                                            APPROVED
                                                        </span>
                
                                                    </div>
                
                                                    <div style="
                                                        width:65px;
                                                        height:5px;
                                                        background:#F36F21;
                                                        margin:20px 0;
                                                        border-radius:5px;
                                                    "></div>
                
                                                </td>
                
                
                                                <td width="45%%"
                                                    align="center"
                                                    valign="middle">
                
                                                    <div style="
                                                        font-size:85px;
                                                        line-height:95px;
                                                    ">
                
                                                        🏪
                
                                                    </div>
                
                                                    <div style="
                                                        display:inline-block;
                                                        padding:10px 18px;
                                                        background:#2E9B50;
                                                        border-radius:8px;
                                                        color:#FFFFFF;
                                                        font-size:15px;
                                                        font-weight:bold;
                                                    ">
                
                                                        ✓ ACCOUNT APPROVED
                
                                                    </div>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= GREETING ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:35px 35px 15px;
                                    ">
                
                                        <h2 style="
                                            margin:0 0 15px;
                                            color:#F36F21;
                                            font-size:27px;
                                        ">
                
                                            Hi %s,
                
                                        </h2>
                
                                        <p style="
                                            margin:0;
                                            font-size:17px;
                                            line-height:29px;
                                            color:#444444;
                                        ">
                
                                            Congratulations! Your merchant account
                                            has been approved by
                                            <b>Jippy Food Delivery</b>.
                
                                        </p>
                
                                        <p style="
                                            margin:12px 0 0;
                                            font-size:17px;
                                            line-height:29px;
                                            color:#444444;
                                        ">
                
                                            You can now add your outlet and start
                                            your journey with Jippy Food Delivery.
                
                                        </p>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= WHAT'S NEXT ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:20px 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   background:#F5FFF7;
                                                   border:1px solid #D6EEDB;
                                                   border-radius:12px;
                                                   overflow:hidden;
                                               ">
                
                                            <tr>
                
                                                <td style="
                                                    padding:25px;
                                                ">
                
                                                    <div style="
                                                        font-size:23px;
                                                        font-weight:bold;
                                                        color:#2E9B50;
                                                        margin-bottom:18px;
                                                    ">
                
                                                        🏪
                                                        &nbsp; What's Next?
                
                                                    </div>
                
                
                                                    <p style="
                                                        margin:11px 0;
                                                        font-size:16px;
                                                        line-height:27px;
                                                        color:#444444;
                                                    ">
                
                                                        <span style="
                                                            color:#2E9B50;
                                                            font-weight:bold;
                                                            font-size:19px;
                                                        ">
                                                            ✓
                                                        </span>
                
                                                        &nbsp; Add your outlet details
                
                                                    </p>
                
                
                                                    <p style="
                                                        margin:11px 0;
                                                        font-size:16px;
                                                        line-height:27px;
                                                        color:#444444;
                                                    ">
                
                                                        <span style="
                                                            color:#2E9B50;
                                                            font-weight:bold;
                                                            font-size:19px;
                                                        ">
                                                            ✓
                                                        </span>
                
                                                        &nbsp; Complete outlet
                                                        verification
                
                                                    </p>
                
                
                                                    <p style="
                                                        margin:11px 0;
                                                        font-size:16px;
                                                        line-height:27px;
                                                        color:#444444;
                                                    ">
                
                                                        <span style="
                                                            color:#2E9B50;
                                                            font-weight:bold;
                                                            font-size:19px;
                                                        ">
                                                            ✓
                                                        </span>
                
                                                        &nbsp; Add your menu/products
                
                                                    </p>
                
                
                                                    <p style="
                                                        margin:11px 0;
                                                        font-size:16px;
                                                        line-height:27px;
                                                        color:#444444;
                                                    ">
                
                                                        <span style="
                                                            color:#2E9B50;
                                                            font-weight:bold;
                                                            font-size:19px;
                                                        ">
                                                            ✓
                                                        </span>
                
                                                        &nbsp; Go online and start
                                                        receiving orders
                
                                                    </p>
                
                
                                                    <!-- BUTTON -->
                
                                                    <table width="100%%"
                                                           cellpadding="0"
                                                           cellspacing="0"
                                                           style="margin-top:25px;">
                
                                                        <tr>
                
                                                            <td align="center">
                
                                                                <div style="
                                                                    display:inline-block;
                                                                    background:#F36F21;
                                                                    color:#FFFFFF;
                                                                    padding:16px 38px;
                                                                    border-radius:8px;
                                                                    font-size:17px;
                                                                    font-weight:bold;
                                                                ">
                
                                                                    ADD YOUR OUTLET
                                                                    &nbsp; →
                
                                                                </div>
                
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                    <p style="
                                                        margin:14px 0 0;
                                                        text-align:center;
                                                        font-size:12px;
                                                        color:#777777;
                                                    ">
                
                                                        Please log in to your
                                                        Jippy Food Delivery account
                                                        to add your outlet.
                
                                                    </p>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= JIPPY PROMISE ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   background:#F2FFF4;
                                                   border:1px solid #CDE8D2;
                                                   border-radius:12px;
                                               ">
                
                                            <tr>
                
                                                <td style="padding:25px;">
                
                                                    <table width="100%%"
                                                           cellpadding="0"
                                                           cellspacing="0">
                
                                                        <tr>
                
                                                            <td width="65"
                                                                valign="top">
                
                                                                <div style="
                                                                    width:50px;
                                                                    height:50px;
                                                                    line-height:50px;
                                                                    text-align:center;
                                                                    background:#2E9B50;
                                                                    border-radius:50%%;
                                                                    font-size:25px;
                                                                ">
                
                                                                    🤝
                
                                                                </div>
                
                                                            </td>
                
                                                            <td>
                
                                                                <div style="
                                                                    font-size:22px;
                                                                    font-weight:bold;
                                                                    color:#2E9B50;
                                                                    margin-bottom:8px;
                                                                ">
                
                                                                    Jippy Promise
                
                                                                </div>
                
                                                                <div style="
                                                                    font-size:18px;
                                                                    font-weight:bold;
                                                                    color:#222222;
                                                                ">
                
                                                                    No Commission.
                                                                    No Deductions.
                
                                                                </div>
                
                                                                <p style="
                                                                    margin:8px 0 0;
                                                                    font-size:15px;
                                                                    line-height:25px;
                                                                    color:#555555;
                                                                ">
                
                                                                    Jippy Food Delivery
                                                                    is not collecting any
                                                                    commission.
                
                                                                    <br>
                
                                                                    Jippy Food Delivery
                                                                    settles the merchant
                                                                    price agreed with you,
                                                                    with no deductions.
                
                                                                </p>
                
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= IMPORTANT REMINDER ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   background:#FFF8E7;
                                                   border:1px solid #F2D58B;
                                                   border-radius:10px;
                                               ">
                
                                            <tr>
                
                                                <td style="padding:20px;">
                
                                                    <div style="
                                                        font-size:19px;
                                                        font-weight:bold;
                                                        color:#D96B00;
                                                    ">
                
                                                        🔔 Important Reminder
                
                                                    </div>
                
                                                    <p style="
                                                        margin:8px 0 0;
                                                        font-size:15px;
                                                        line-height:25px;
                                                        color:#555555;
                                                    ">
                
                                                        Please complete your outlet
                                                        registration and verification
                                                        to continue your onboarding
                                                        journey with Jippy Food Delivery.
                
                                                    </p>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= SUPPORT ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   background:#F1F7FF;
                                                   border:1px solid #C9DDF5;
                                                   border-radius:10px;
                                               ">
                
                                            <tr>
                
                                                <td style="padding:22px;">
                
                                                    <table width="100%%"
                                                           cellpadding="0"
                                                           cellspacing="0">
                
                                                        <tr>
                
                                                            <td width="60"
                                                                valign="middle">
                
                                                                <div style="
                                                                    width:45px;
                                                                    height:45px;
                                                                    line-height:45px;
                                                                    text-align:center;
                                                                    background:#E3F0FF;
                                                                    border-radius:50%%;
                                                                    font-size:23px;
                                                                ">
                
                                                                    🎧
                
                                                                </div>
                
                                                            </td>
                
                                                            <td>
                
                                                                <div style="
                                                                    font-size:20px;
                                                                    font-weight:bold;
                                                                    color:#1769AA;
                                                                ">
                
                                                                    Need help?
                
                                                                </div>
                
                                                                <p style="
                                                                    margin:5px 0 0;
                                                                    font-size:14px;
                                                                    color:#555555;
                                                                ">
                
                                                                    Our team is happy
                                                                    to help you.
                
                                                                </p>
                
                                                            </td>
                
                                                            <td align="right"
                                                                valign="middle">
                
                                                                <a href="mailto:support@jippymart.in"
                                                                   style="
                                                                       color:#1769AA;
                                                                       text-decoration:none;
                                                                       font-weight:bold;
                                                                       font-size:14px;
                                                                   ">
                
                                                                    ✉
                                                                    support@jippymart.in
                
                                                                </a>
                
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= FOOTER ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:28px 20px;
                                        background:#1E2227;
                                        text-align:center;
                                        color:#FFFFFF;
                                    ">
                
                                        <div style="
                                            font-size:25px;
                                            font-weight:bold;
                                            margin-bottom:14px;
                                        ">
                
                                            🛍️
                
                                            <span style="color:#FFFFFF;">
                                                Jippy
                                            </span>
                
                                            <span style="color:#F36F21;">
                                                Food Delivery
                                            </span>
                
                                        </div>
                
                                        <p style="
                                            margin:5px 0;
                                            font-size:13px;
                                            color:#CCCCCC;
                                        ">
                
                                            © 2026 Jippy Technologies Pvt. Ltd.
                
                                        </p>
                
                                        <p style="
                                            margin:5px 0 15px;
                                            font-size:13px;
                                            color:#CCCCCC;
                                        ">
                
                                            All Rights Reserved.
                
                                        </p>
                
                                        <p style="
                                            margin:0;
                                            font-size:12px;
                                            color:#AAAAAA;
                                        ">
                
                                            Privacy Policy
                                            &nbsp; | &nbsp;
                                            Terms & Conditions
                                            &nbsp; | &nbsp;
                                            Support
                
                                        </p>
                
                                    </td>
                
                                </tr>
                
                            </table>
                
                        </td>
                    </tr>
                
                </table>
                
                </body>
                </html>
                """.formatted(merchantName);
    }

    private String buildMerchantRegistrationTemplate(String merchantName, String merchantEmail) {

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                
                    <title>Welcome to Jippy Food Delivery</title>
                </head>
                
                <body style="
                    margin:0;
                    padding:0;
                    background:#F4F6F8;
                    font-family:Arial,Helvetica,sans-serif;
                    color:#222222;
                ">
                
                <table width="100%%"
                       cellpadding="0"
                       cellspacing="0"
                       border="0"
                       style="background:#F4F6F8;">
                
                    <tr>
                        <td align="center" style="padding:25px 10px;">
                
                            <!-- MAIN CONTAINER -->
                            <table width="720"
                                   cellpadding="0"
                                   cellspacing="0"
                                   border="0"
                                   style="
                                       width:100%%;
                                       max-width:720px;
                                       background:#FFFFFF;
                                       border-radius:14px;
                                       overflow:hidden;
                                       border:1px solid #E5E5E5;
                                   ">
                
                                <!-- ================= HEADER ================= -->
                
                                <tr>
                                    <td style="
                                        padding:22px 30px;
                                        background:#FFFFFF;
                                        border-bottom:1px solid #EEEEEE;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0">
                
                                            <tr>
                
                                                <td align="left">
                
                                                    <img
                                                        src="https://s3-mmrcdl1987.s3.ap-south-1.amazonaws.com/unnamed.png"
                                                        width="170"
                                                        alt="Jippy Food Delivery"
                                                        style="
                                                            display:block;
                                                            border:0;
                                                            max-width:170px;
                                                        "
                                                    >
                
                                                </td>
                
                                                <td align="right"
                                                    style="
                                                        font-size:13px;
                                                        color:#555555;
                                                    ">
                
                                                    ✉
                                                    support@jippymart.in
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                                </tr>
                
                
                                <!-- ================= HERO ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:45px 35px;
                                        background:#FFF6EE;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0">
                
                                            <tr>
                
                                                <td width="55%%"
                                                    valign="middle"
                                                    style="padding-right:15px;">
                
                                                    <div style="
                                                        font-size:38px;
                                                        line-height:46px;
                                                        font-weight:bold;
                                                        color:#222222;
                                                    ">
                
                                                        Welcome to
                
                                                    </div>
                
                                                    <div style="
                                                        font-size:42px;
                                                        line-height:50px;
                                                        font-weight:bold;
                                                        color:#F36F21;
                                                    ">
                
                                                        Jippy Food Delivery!
                
                                                    </div>
                
                                                    <p style="
                                                        margin:18px 0 0;
                                                        font-size:21px;
                                                        line-height:31px;
                                                        font-weight:bold;
                                                        color:#222222;
                                                    ">
                
                                                        Your merchant registration
                                                        was successful.
                
                                                    </p>
                
                                                    <div style="
                                                        width:65px;
                                                        height:5px;
                                                        background:#F36F21;
                                                        margin:20px 0;
                                                        border-radius:5px;
                                                    "></div>
                
                                                </td>
                
                
                                                <td width="45%%"
                                                    align="center"
                                                    valign="middle">
                
                                                    <div style="
                                                        font-size:90px;
                                                        line-height:100px;
                                                    ">
                
                                                        🏪
                
                                                    </div>
                
                                                    <div style="
                                                        display:inline-block;
                                                        padding:9px 18px;
                                                        background:#2E9B50;
                                                        border-radius:8px;
                                                        color:#FFFFFF;
                                                        font-size:15px;
                                                        font-weight:bold;
                                                    ">
                
                                                        ✓ REGISTRATION RECEIVED
                
                                                    </div>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= GREETING ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:35px 35px 15px;
                                    ">
                
                                        <h2 style="
                                            margin:0 0 15px;
                                            color:#F36F21;
                                            font-size:27px;
                                        ">
                
                                            Hi %s,
                
                                        </h2>
                
                                        <p style="
                                            margin:0;
                                            font-size:17px;
                                            line-height:29px;
                                            color:#444444;
                                        ">
                
                                            Thank you for registering your business
                                            with <b>Jippy Food Delivery</b>.
                
                                        </p>
                
                                        <p style="
                                            margin:12px 0 0;
                                            font-size:17px;
                                            line-height:29px;
                                            color:#444444;
                                        ">
                
                                            We have successfully received your
                                            merchant registration. Our team will
                                            review your details and notify you once
                                            your merchant account is approved.
                
                                        </p>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= MERCHANT DETAILS ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:20px 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   border:1px solid #E5E5E5;
                                                   border-radius:10px;
                                                   overflow:hidden;
                                               ">
                
                                            <tr>
                
                                                <td style="
                                                    padding:20px;
                                                    background:#FFF8F2;
                                                    color:#F36F21;
                                                    font-size:21px;
                                                    font-weight:bold;
                                                ">
                
                                                    🏪
                                                    &nbsp; Merchant Details
                
                                                </td>
                
                                            </tr>
                
                                            <tr>
                
                                                <td style="padding:20px;">
                
                                                    <table width="100%%"
                                                           cellpadding="0"
                                                           cellspacing="0">
                
                                                        <tr>
                
                                                            <td width="42%%"
                                                                style="
                                                                    padding:9px 0;
                                                                    color:#666666;
                                                                    font-weight:bold;
                                                                ">
                
                                                                Merchant Name
                
                                                            </td>
                
                                                            <td style="
                                                                padding:9px 0;
                                                                color:#222222;
                                                            ">
                
                                                                %s
                
                                                            </td>
                
                                                        </tr>
                
                                                        <tr>
                
                                                            <td style="
                                                                padding:9px 0;
                                                                color:#666666;
                                                                font-weight:bold;
                                                            ">
                
                                                                Email
                
                                                            </td>
                
                                                            <td style="
                                                                padding:9px 0;
                                                                color:#222222;
                                                            ">
                
                                                                %s
                
                                                            </td>
                
                                                        </tr>
                
                                                        <tr>
                
                                                            <td style="
                                                                padding:9px 0;
                                                                color:#666666;
                                                                font-weight:bold;
                                                            ">
                
                                                                Status
                
                                                            </td>
                
                                                            <td style="
                                                                padding:9px 0;
                                                            ">
                
                                                                <span style="
                                                                    display:inline-block;
                                                                    padding:6px 12px;
                                                                    background:#FFF0C2;
                                                                    color:#8A5A00;
                                                                    border-radius:5px;
                                                                    font-size:13px;
                                                                    font-weight:bold;
                                                                ">
                
                                                                    PENDING APPROVAL
                
                                                                </span>
                
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= WHAT'S NEXT ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   background:#F7FFF8;
                                                   border:1px solid #D8EEDB;
                                                   border-radius:10px;
                                               ">
                
                                            <tr>
                
                                                <td style="
                                                    padding:22px;
                                                ">
                
                                                    <div style="
                                                        font-size:21px;
                                                        font-weight:bold;
                                                        color:#2E9B50;
                                                        margin-bottom:12px;
                                                    ">
                
                                                        📋
                                                        &nbsp; What's Next?
                
                                                    </div>
                
                                                    <p style="
                                                        margin:8px 0;
                                                        font-size:15px;
                                                        line-height:25px;
                                                        color:#444444;
                                                    ">
                
                                                        ✓ Our team will review your
                                                        merchant information.
                
                                                    </p>
                
                                                    <p style="
                                                        margin:8px 0;
                                                        font-size:15px;
                                                        line-height:25px;
                                                        color:#444444;
                                                    ">
                
                                                        ✓ Once approved, you will
                                                        receive an approval email.
                
                                                    </p>
                
                                                    <p style="
                                                        margin:8px 0;
                                                        font-size:15px;
                                                        line-height:25px;
                                                        color:#444444;
                                                    ">
                
                                                        ✓ You can then add your
                                                        outlet and continue your
                                                        onboarding.
                
                                                    </p>
                
                                                    <p style="
                                                        margin:8px 0 0;
                                                        font-size:15px;
                                                        line-height:25px;
                                                        color:#444444;
                                                    ">
                
                                                        ✓ After your outlet is
                                                        activated, you can start
                                                        receiving orders.
                
                                                    </p>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= JIPPY PROMISE ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   border:1px solid #D8EEDB;
                                                   border-radius:10px;
                                                   overflow:hidden;
                                               ">
                
                                            <tr>
                
                                                <td style="
                                                    padding:20px;
                                                    background:#F2FFF4;
                                                ">
                
                                                    <div style="
                                                        font-size:21px;
                                                        font-weight:bold;
                                                        color:#2E9B50;
                                                    ">
                
                                                        🛡️
                                                        &nbsp; Jippy Promise
                
                                                    </div>
                
                                                </td>
                
                                            </tr>
                
                                            <tr>
                
                                                <td style="
                                                    padding:20px;
                                                ">
                
                                                    <div style="
                                                        font-size:18px;
                                                        font-weight:bold;
                                                        color:#222222;
                                                    ">
                
                                                        💰 No Commission. No
                                                        Deductions.
                
                                                    </div>
                
                                                    <p style="
                                                        margin:8px 0 0;
                                                        font-size:15px;
                                                        line-height:25px;
                                                        color:#555555;
                                                    ">
                
                                                        Jippy is not collecting any
                                                        commission. Jippy settles
                                                        the merchant price agreed
                                                        with you, with no
                                                        deductions.
                
                                                    </p>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= IMPORTANT NOTE ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 35px 35px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   background:#FFF8E7;
                                                   border:1px solid #F2D58B;
                                                   border-radius:8px;
                                               ">
                
                                            <tr>
                
                                                <td style="
                                                    padding:18px 20px;
                                                ">
                
                                                    <span style="
                                                        font-size:18px;
                                                    ">
                
                                                        🔔
                
                                                    </span>
                
                                                    <b style="
                                                        color:#D96B00;
                                                        font-size:15px;
                                                    ">
                
                                                        &nbsp; Important Note
                
                                                    </b>
                
                                                    <p style="
                                                        margin:7px 0 0;
                                                        font-size:14px;
                                                        line-height:23px;
                                                        color:#555555;
                                                    ">
                
                                                        Please keep this email safe.
                                                        We will notify you as soon
                                                        as your merchant account
                                                        is approved.
                
                                                    </p>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= SUPPORT ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:25px 35px;
                                        background:#F8F8F8;
                                        text-align:center;
                                    ">
                
                                        <p style="
                                            margin:0;
                                            font-size:15px;
                                            line-height:25px;
                                            color:#555555;
                                        ">
                
                                            Need help?
                
                                            <b style="color:#F36F21;">
                                                support@jippymart.in
                                            </b>
                
                                        </p>
                
                                        <p style="
                                            margin:7px 0 0;
                                            font-size:13px;
                                            color:#777777;
                                        ">
                
                                            Our team is happy to help you.
                
                                        </p>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= FOOTER ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:25px 20px;
                                        background:#1E2227;
                                        text-align:center;
                                        color:#FFFFFF;
                                    ">
                
                                        <div style="
                                            font-size:24px;
                                            font-weight:bold;
                                            margin-bottom:12px;
                                        ">
                
                                            🛍️
                
                                            <span style="color:#FFFFFF;">
                                                Jippy
                                            </span>
                
                                            <span style="color:#F36F21;">
                                                Food Delivery
                                            </span>
                
                                        </div>
                
                                        <p style="
                                            margin:5px 0;
                                            font-size:13px;
                                            color:#CCCCCC;
                                        ">
                
                                            © 2026 Jippy Technologies Pvt. Ltd.
                                            All Rights Reserved.
                
                                        </p>
                
                                        <p style="
                                            margin:10px 0 0;
                                            font-size:12px;
                                            color:#AAAAAA;
                                        ">
                
                                            Privacy Policy
                                            &nbsp; | &nbsp;
                                            Terms & Conditions
                                            &nbsp; | &nbsp;
                                            Support
                
                                        </p>
                
                                    </td>
                
                                </tr>
                
                            </table>
                
                        </td>
                    </tr>
                
                </table>
                
                </body>
                </html>
                """.formatted(merchantName, merchantName, merchantEmail);
    }


    private String buildOutletOnlineTemplate(String outletName, String merchantName, String goLiveDate) {

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                
                    <title>Your Outlet is Now Online - Jippy Mart</title>
                </head>
                
                <body style="
                    margin:0;
                    padding:0;
                    background:#F4F6F8;
                    font-family:Arial,Helvetica,sans-serif;
                    color:#222222;
                ">
                
                <table width="100%%"
                       cellpadding="0"
                       cellspacing="0"
                       border="0"
                       style="background:#F4F6F8;">
                
                    <tr>
                        <td align="center"
                            style="padding:25px 10px;">
                
                            <!-- MAIN CONTAINER -->
                
                            <table width="720"
                                   cellpadding="0"
                                   cellspacing="0"
                                   border="0"
                                   style="
                                       width:100%%;
                                       max-width:720px;
                                       background:#FFFFFF;
                                       border-radius:14px;
                                       overflow:hidden;
                                       border:1px solid #E5E5E5;
                                   ">
                
                                <!-- ================= HEADER ================= -->
                
                                <tr>
                                    <td style="
                                        padding:22px 30px;
                                        background:#FFFFFF;
                                        border-bottom:1px solid #EEEEEE;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0">
                
                                            <tr>
                
                                                <td align="left">
                
                                                    <img
                                                        src="https://s3-mmrcdl1987.s3.ap-south-1.amazonaws.com/unnamed.png"
                                                        width="170"
                                                        alt="Jippy Mart"
                                                        style="
                                                            display:block;
                                                            border:0;
                                                            max-width:170px;
                                                        "
                                                    >
                
                                                </td>
                
                                                <td align="right"
                                                    style="
                                                        font-size:13px;
                                                        color:#555555;
                                                    ">
                
                                                    ✉
                                                    support@jippymart.in
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                                </tr>
                
                
                                <!-- ================= HERO ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:45px 35px;
                                        background:#EEF7FF;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0">
                
                                            <tr>
                
                                                <td width="52%%"
                                                    valign="middle"
                                                    style="
                                                        padding-right:15px;
                                                    ">
                
                                                    <div style="
                                                        font-size:38px;
                                                        line-height:46px;
                                                        font-weight:bold;
                                                        color:#222222;
                                                    ">
                
                                                        Your Outlet is
                
                                                    </div>
                
                                                    <div style="
                                                        font-size:40px;
                                                        line-height:48px;
                                                        font-weight:bold;
                                                        color:#F36F21;
                                                    ">
                
                                                        Now Online!
                
                                                    </div>
                
                                                    <div style="
                                                        width:65px;
                                                        height:5px;
                                                        background:#2E9B50;
                                                        margin:18px 0;
                                                        border-radius:5px;
                                                    "></div>
                
                                                    <div style="
                                                        font-size:20px;
                                                        line-height:30px;
                                                        font-weight:bold;
                                                        color:#2E9B50;
                                                    ">
                
                                                        Congratulations!
                
                                                    </div>
                
                                                    <p style="
                                                        margin:8px 0 0;
                                                        font-size:17px;
                                                        line-height:28px;
                                                        color:#333333;
                                                    ">
                
                                                        Your outlet is now live on
                                                        <b>Jippy Mart</b>.
                
                                                    </p>
                
                                                    <p style="
                                                        margin:8px 0 0;
                                                        font-size:16px;
                                                        line-height:27px;
                                                        color:#444444;
                                                    ">
                
                                                        Customers can now discover
                                                        your outlet and place orders.
                
                                                    </p>
                
                                                </td>
                
                
                                                <td width="48%%"
                                                    align="center"
                                                    valign="middle">
                
                                                    <div style="
                                                        font-size:95px;
                                                        line-height:100px;
                                                    ">
                
                                                        🏪
                
                                                    </div>
                
                                                    <div style="
                                                        display:inline-block;
                                                        padding:9px 22px;
                                                        background:#159447;
                                                        border-radius:8px;
                                                        color:#FFFFFF;
                                                        font-size:20px;
                                                        font-weight:bold;
                                                    ">
                
                                                        ● LIVE
                
                                                    </div>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= GREETING ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:30px 35px 10px;
                                    ">
                
                                        <h2 style="
                                            margin:0 0 12px;
                                            color:#F36F21;
                                            font-size:25px;
                                        ">
                
                                            Hi %s,
                
                                        </h2>
                
                                        <p style="
                                            margin:0;
                                            font-size:17px;
                                            line-height:29px;
                                            color:#444444;
                                        ">
                
                                            Great news! Your outlet has been
                                            successfully activated and is now
                                            <b style="color:#159447;">
                                                ONLINE
                                            </b>
                                            on Jippy Mart.
                
                                        </p>
                
                                        <p style="
                                            margin:10px 0 0;
                                            font-size:17px;
                                            line-height:29px;
                                            color:#444444;
                                        ">
                
                                            Start managing your orders and grow
                                            your business with Jippy Mart.
                
                                            <span style="font-size:20px;">
                                                🤝
                                            </span>
                
                                        </p>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= DETAILS + NEXT STEPS ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:20px 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0">
                
                                            <tr>
                
                                                <!-- OUTLET DETAILS -->
                
                                                <td width="48%%"
                                                    valign="top"
                                                    style="
                                                        border:1px solid #E5E5E5;
                                                        border-radius:10px;
                                                        padding:22px;
                                                    ">
                
                                                    <div style="
                                                        font-size:19px;
                                                        font-weight:bold;
                                                        color:#F36F21;
                                                        margin-bottom:18px;
                                                    ">
                
                                                        🏪
                                                        &nbsp; OUTLET DETAILS
                
                                                    </div>
                
                                                    <p style="
                                                        margin:9px 0;
                                                        font-size:15px;
                                                    ">
                
                                                        <b>Outlet Name</b>
                                                        &nbsp;&nbsp;:
                                                        %s
                
                                                    </p>
                
                                                    <p style="
                                                        margin:9px 0;
                                                        font-size:15px;
                                                    ">
                
                                                        <b>Merchant Name</b>
                                                        &nbsp;:
                                                        %s
                
                                                    </p>
                
                                                    <p style="
                                                        margin:9px 0;
                                                        font-size:15px;
                                                    ">
                
                                                        <b>Status</b>
                                                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:
                
                                                        <span style="
                                                            color:#159447;
                                                            font-weight:bold;
                                                        ">
                                                            ONLINE
                                                        </span>
                
                                                    </p>
                
                                                    <p style="
                                                        margin:9px 0;
                                                        font-size:15px;
                                                    ">
                
                                                        <b>Go Live Date</b>
                                                        &nbsp;:
                                                        %s
                
                                                    </p>
                
                                                </td>
                
                
                                                <td width="4%%">
                                                    &nbsp;
                                                </td>
                
                
                                                <!-- NEXT STEPS -->
                
                                                <td width="48%%"
                                                    valign="top"
                                                    style="
                                                        border:1px solid #E5E5E5;
                                                        border-radius:10px;
                                                        padding:22px;
                                                    ">
                
                                                    <div style="
                                                        font-size:19px;
                                                        font-weight:bold;
                                                        color:#159447;
                                                        margin-bottom:18px;
                                                    ">
                
                                                        🎯
                                                        &nbsp; NEXT STEPS
                
                                                    </div>
                
                                                    <p style="
                                                        margin:9px 0;
                                                        font-size:15px;
                                                        line-height:23px;
                                                    ">
                
                                                        ✓ Manage your menu and stock
                
                                                    </p>
                
                                                    <p style="
                                                        margin:9px 0;
                                                        font-size:15px;
                                                        line-height:23px;
                                                    ">
                
                                                        ✓ Accept and fulfill orders
                
                                                    </p>
                
                                                    <p style="
                                                        margin:9px 0;
                                                        font-size:15px;
                                                        line-height:23px;
                                                    ">
                
                                                        ✓ Provide the best quality
                                                        and service
                
                                                    </p>
                
                                                    <p style="
                                                        margin:9px 0;
                                                        font-size:15px;
                                                        line-height:23px;
                                                    ">
                
                                                        ✓ Grow your business with
                                                        Jippy Mart
                
                                                    </p>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= JIPPY POLICIES ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   border:1px solid #F36F21;
                                                   border-radius:10px;
                                                   overflow:hidden;
                                               ">
                
                                            <!-- POLICY HEADER -->
                
                                            <tr>
                
                                                <td style="
                                                    padding:20px;
                                                    text-align:center;
                                                    background:#FFF8F2;
                                                ">
                
                                                    <span style="
                                                        font-size:22px;
                                                        font-weight:bold;
                                                        color:#F36F21;
                                                    ">
                
                                                        🛡️ JIPPY POLICIES
                
                                                    </span>
                
                                                </td>
                
                                            </tr>
                
                
                                            <!-- POLICY 1 -->
                
                                            <tr>
                
                                                <td style="
                                                    padding:20px;
                                                    border-top:1px dashed #DDDDDD;
                                                ">
                
                                                    <table width="100%%">
                
                                                        <tr>
                
                                                            <td width="55"
                                                                valign="top">
                
                                                                <div style="
                                                                    width:40px;
                                                                    height:40px;
                                                                    line-height:40px;
                                                                    text-align:center;
                                                                    background:#EAF8E8;
                                                                    border-radius:50%%;
                                                                    font-size:21px;
                                                                ">
                
                                                                    💰
                
                                                                </div>
                
                                                            </td>
                
                                                            <td>
                
                                                                <b style="
                                                                    font-size:16px;
                                                                    color:#222222;
                                                                ">
                
                                                                    No Commission.
                                                                    No Deductions.
                
                                                                </b>
                
                                                                <p style="
                                                                    margin:6px 0 0;
                                                                    font-size:14px;
                                                                    line-height:23px;
                                                                    color:#555555;
                                                                ">
                
                                                                    Jippy is not
                                                                    collecting any
                                                                    commission.
                                                                    Jippy settles the
                                                                    merchant price
                                                                    agreed with you,
                                                                    with no deductions.
                
                                                                </p>
                
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                </td>
                
                                            </tr>
                
                
                                            <!-- POLICY 2 -->
                
                                            <tr>
                
                                                <td style="
                                                    padding:20px;
                                                    border-top:1px dashed #DDDDDD;
                                                ">
                
                                                    <table width="100%%">
                
                                                        <tr>
                
                                                            <td width="55"
                                                                valign="top">
                
                                                                <div style="
                                                                    width:40px;
                                                                    height:40px;
                                                                    line-height:40px;
                                                                    text-align:center;
                                                                    background:#FFF1E4;
                                                                    border-radius:50%%;
                                                                    font-size:21px;
                                                                ">
                
                                                                    🕐
                
                                                                </div>
                
                                                            </td>
                
                                                            <td>
                
                                                                <b style="
                                                                    font-size:16px;
                                                                ">
                
                                                                    Don't Close
                                                                    Outlet Before
                                                                    Timings.
                
                                                                </b>
                
                                                                <p style="
                                                                    margin:6px 0 0;
                                                                    font-size:14px;
                                                                    line-height:23px;
                                                                    color:#555555;
                                                                ">
                
                                                                    Please don't
                                                                    close your
                                                                    outlet before
                                                                    your configured
                                                                    outlet timings.
                                                                    Your orders may
                                                                    get impacted.
                
                                                                </p>
                
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                </td>
                
                                            </tr>
                
                
                                            <!-- POLICY 3 -->
                
                                            <tr>
                
                                                <td style="
                                                    padding:20px;
                                                    border-top:1px dashed #DDDDDD;
                                                ">
                
                                                    <table width="100%%">
                
                                                        <tr>
                
                                                            <td width="55"
                                                                valign="top">
                
                                                                <div style="
                                                                    width:40px;
                                                                    height:40px;
                                                                    line-height:40px;
                                                                    text-align:center;
                                                                    background:#EEF5FF;
                                                                    border-radius:50%%;
                                                                    font-size:21px;
                                                                ">
                
                                                                    🛡️
                
                                                                </div>
                
                                                            </td>
                
                                                            <td>
                
                                                                <b style="
                                                                    font-size:16px;
                                                                ">
                
                                                                    Maintain
                                                                    Outlet Hygiene.
                
                                                                </b>
                
                                                                <p style="
                                                                    margin:6px 0 0;
                                                                    font-size:14px;
                                                                    line-height:23px;
                                                                    color:#555555;
                                                                ">
                
                                                                    Please maintain
                                                                    good outlet and
                                                                    food hygiene to
                                                                    help avoid food
                                                                    spoilage and
                                                                    quality issues.
                
                                                                </p>
                
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                </td>
                
                                            </tr>
                
                
                                            <!-- POLICY 4 -->
                
                                            <tr>
                
                                                <td style="
                                                    padding:20px;
                                                    border-top:1px dashed #DDDDDD;
                                                ">
                
                                                    <table width="100%%">
                
                                                        <tr>
                
                                                            <td width="55"
                                                                valign="top">
                
                                                                <div style="
                                                                    width:40px;
                                                                    height:40px;
                                                                    line-height:40px;
                                                                    text-align:center;
                                                                    background:#F4ECFF;
                                                                    border-radius:50%%;
                                                                    font-size:21px;
                                                                ">
                
                                                                    📦
                
                                                                </div>
                
                                                            </td>
                
                                                            <td>
                
                                                                <b style="
                                                                    font-size:16px;
                                                                ">
                
                                                                    Try to Fulfill
                                                                    All Orders.
                
                                                                </b>
                
                                                                <p style="
                                                                    margin:6px 0 0;
                                                                    font-size:14px;
                                                                    line-height:23px;
                                                                    color:#555555;
                                                                ">
                
                                                                    Please try to
                                                                    fulfill all
                                                                    orders you
                                                                    receive.
                                                                    Rejected orders
                                                                    may impact your
                                                                    outlet
                                                                    performance.
                
                                                                    <br>
                
                                                                    Our AI analyzes
                                                                    outlet
                                                                    performance,
                                                                    and repeated
                                                                    rejections may
                                                                    reduce future
                                                                    orders.
                
                                                                </p>
                
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                </td>
                
                                            </tr>
                
                
                                            <!-- POLICY 5 -->
                
                                            <tr>
                
                                                <td style="
                                                    padding:20px;
                                                    border-top:1px dashed #DDDDDD;
                                                ">
                
                                                    <table width="100%%">
                
                                                        <tr>
                
                                                            <td width="55"
                                                                valign="top">
                
                                                                <div style="
                                                                    width:40px;
                                                                    height:40px;
                                                                    line-height:40px;
                                                                    text-align:center;
                                                                    background:#FFF3E0;
                                                                    border-radius:50%%;
                                                                    font-size:21px;
                                                                ">
                
                                                                    📦
                
                                                                </div>
                
                                                            </td>
                
                                                            <td>
                
                                                                <b style="
                                                                    font-size:16px;
                                                                ">
                
                                                                    Manage Stock
                                                                    Properly.
                
                                                                </b>
                
                                                                <p style="
                                                                    margin:6px 0 0;
                                                                    font-size:14px;
                                                                    line-height:23px;
                                                                    color:#555555;
                                                                ">
                
                                                                    If you are
                                                                    facing any
                                                                    stock issues,
                                                                    please turn
                                                                    off that
                                                                    product until
                                                                    stock is
                                                                    available.
                
                                                                    <br>
                
                                                                    Receiving an
                                                                    order and then
                                                                    cancelling it
                                                                    is not good for
                                                                    your outlet
                                                                    performance.
                
                                                                </p>
                
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                </td>
                
                                            </tr>
                
                
                                            <!-- POLICY 6 -->
                
                                            <tr>
                
                                                <td style="
                                                    padding:20px;
                                                    border-top:1px dashed #DDDDDD;
                                                ">
                
                                                    <table width="100%%">
                
                                                        <tr>
                
                                                            <td width="55"
                                                                valign="top">
                
                                                                <div style="
                                                                    width:40px;
                                                                    height:40px;
                                                                    line-height:40px;
                                                                    text-align:center;
                                                                    background:#E8F7F4;
                                                                    border-radius:50%%;
                                                                    font-size:21px;
                                                                ">
                
                                                                    💳
                
                                                                </div>
                
                                                            </td>
                
                                                            <td>
                
                                                                <b style="
                                                                    font-size:16px;
                                                                ">
                
                                                                    Check Your
                                                                    Settlements
                                                                    Every Week.
                
                                                                </b>
                
                                                                <p style="
                                                                    margin:6px 0 0;
                                                                    font-size:14px;
                                                                    line-height:23px;
                                                                    color:#555555;
                                                                ">
                
                                                                    Please check
                                                                    your settlement
                                                                    details every
                                                                    week.
                
                                                                    <br>
                
                                                                    If you have any
                                                                    questions or
                                                                    concerns about
                                                                    your settlement,
                                                                    please reach us
                                                                    at:
                
                                                                    <b>
                                                                        support@jippymart.in
                                                                    </b>
                
                                                                </p>
                
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= THANK YOU ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               style="
                                                   background:#FFF8E7;
                                                   border:1px solid #F4D79A;
                                                   border-radius:10px;
                                               ">
                
                                            <tr>
                
                                                <td style="
                                                    padding:22px;
                                                    text-align:center;
                                                ">
                
                                                    <div style="
                                                        font-size:30px;
                                                    ">
                
                                                        🏆
                
                                                    </div>
                
                                                    <p style="
                                                        margin:8px 0;
                                                        font-size:16px;
                                                        color:#444444;
                                                    ">
                
                                                        Thank you for being a
                                                        valuable Jippy Mart partner.
                
                                                    </p>
                
                                                    <p style="
                                                        margin:0;
                                                        font-size:18px;
                                                        font-weight:bold;
                                                        color:#F36F21;
                                                    ">
                
                                                        Together, let's deliver
                                                        the best experience
                                                        to our customers!
                
                                                    </p>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= SUPPORT ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:25px 35px;
                                        background:#F8F8F8;
                                        text-align:center;
                                    ">
                
                                        <p style="
                                            margin:0;
                                            font-size:15px;
                                            line-height:25px;
                                            color:#555555;
                                        ">
                
                                            Need help?
                
                                            <b style="color:#F36F21;">
                                                support@jippymart.in
                                            </b>
                
                                        </p>
                
                                        <p style="
                                            margin:7px 0 0;
                                            font-size:13px;
                                            color:#777777;
                                        ">
                
                                            Our team is happy to help you.
                
                                        </p>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- ================= FOOTER ================= -->
                
                                <tr>
                
                                    <td style="
                                        padding:25px 20px;
                                        background:#1E2227;
                                        text-align:center;
                                        color:#FFFFFF;
                                    ">
                
                                        <div style="
                                            font-size:24px;
                                            font-weight:bold;
                                            margin-bottom:12px;
                                        ">
                
                                            🛍️
                                            <span style="color:#FFFFFF;">
                                                Jippy
                                            </span>
                                            <span style="color:#F36F21;">
                                                Mart
                                            </span>
                
                                        </div>
                
                                        <p style="
                                            margin:5px 0;
                                            font-size:13px;
                                            color:#CCCCCC;
                                        ">
                
                                            © 2026 Jippy Technologies Pvt. Ltd.
                                            All Rights Reserved.
                
                                        </p>
                
                                        <p style="
                                            margin:10px 0 0;
                                            font-size:12px;
                                            color:#AAAAAA;
                                        ">
                
                                            Privacy Policy
                                            &nbsp; | &nbsp;
                                            Terms & Conditions
                                            &nbsp; | &nbsp;
                                            Support
                
                                        </p>
                
                                    </td>
                
                                </tr>
                
                            </table>
                
                        </td>
                    </tr>
                
                </table>
                
                </body>
                </html>
                """.formatted(outletName, outletName, merchantName, goLiveDate);
    }

    private String buildSchedulerSummaryTemplate(SchedulerSummaryDto summary) {

        return """
                <!DOCTYPE html>
                <html>
                
                <head>
                    <meta charset="UTF-8">
                    <title>Product Content Scheduler Report</title>
                </head>
                
                <body style="margin:0;padding:30px;background:#f4f6f8;font-family:Arial,Helvetica,sans-serif;">
                
                <table width="700"
                       align="center"
                       cellpadding="0"
                       cellspacing="0"
                       style="background:#ffffff;border:1px solid #dddddd;border-radius:8px;">
                
                    <tr style="background:#2E7D32;">
                        <td style="padding:20px;color:#ffffff;font-size:24px;font-weight:bold;">
                            Product Content Scheduler Report
                        </td>
                    </tr>
                
                    <tr>
                
                        <td style="padding:30px;">
                
                            <p>Hello Admin,</p>
                
                            <p>
                                The <b>Product Content Scheduler</b> has completed successfully.
                                Below is the execution summary for this scheduler run.
                            </p>
                
                            <table width="100%%"
                                   cellpadding="0"
                                   cellspacing="0"
                                   style="border-collapse:collapse;margin-top:20px;">
                
                                <tr style="background:#f8f8f8;">
                                    <td style="padding:10px;border:1px solid #dddddd;font-weight:bold;">
                                        Total Products Processed
                                    </td>
                
                                    <td style="padding:10px;border:1px solid #dddddd;">
                                        %d
                                    </td>
                                </tr>
                
                                <tr>
                                    <td style="padding:10px;border:1px solid #dddddd;font-weight:bold;">
                                        Successfully Updated
                                    </td>
                
                                    <td style="padding:10px;border:1px solid #dddddd;color:#2E7D32;font-weight:bold;">
                                        %d
                                    </td>
                                </tr>
                
                                <tr style="background:#f8f8f8;">
                                    <td style="padding:10px;border:1px solid #dddddd;font-weight:bold;">
                                        Missing Products
                                    </td>
                
                                    <td style="padding:10px;border:1px solid #dddddd;color:#F57C00;font-weight:bold;">
                                        %d
                                    </td>
                                </tr>
                
                                <tr>
                                    <td style="padding:10px;border:1px solid #dddddd;font-weight:bold;">
                                        Failed Products
                                    </td>
                
                                    <td style="padding:10px;border:1px solid #dddddd;color:#D32F2F;font-weight:bold;">
                                        %d
                                    </td>
                                </tr>
                
                                <tr style="background:#f8f8f8;">
                                    <td style="padding:10px;border:1px solid #dddddd;font-weight:bold;">
                                        Pages Processed
                                    </td>
                
                                    <td style="padding:10px;border:1px solid #dddddd;">
                                        %d
                                    </td>
                                </tr>
                
                                <tr>
                                    <td style="padding:10px;border:1px solid #dddddd;font-weight:bold;">
                                        Execution Time
                                    </td>
                
                                    <td style="padding:10px;border:1px solid #dddddd;">
                                        %d ms
                                    </td>
                                </tr>
                
                            </table>
                
                            <br>
                
                            <div style="background:#FFF8E1;
                                        border-left:5px solid #FFB300;
                                        padding:20px;
                                        border-radius:4px;">
                
                                <h3 style="margin-top:0;color:#E65100;">
                                    Action Required
                                </h3>
                
                                <p>
                                    Some products could not be updated during this scheduler execution.
                                    Please review the attached CSV report and complete the following steps:
                                </p>
                
                                <ol style="padding-left:22px;line-height:1.8;">
                
                                    <li>
                                        Open the <b>Product Content Excel</b>.
                                    </li>
                
                                    <li>
                                        Search for the products listed in the attached CSV report.
                                    </li>
                
                                    <li>
                                        Add or update the missing
                                        <b>Product Description</b>.
                                    </li>
                
                                    <li>
                                        Add a valid
                                        <b>Product Image URL</b>
                                        for each product.
                                    </li>
                
                                    <li>
                                        Save the updated Product Content Excel.
                                    </li>
                
                                    <li>
                                        Upload the latest Excel file to the configured
                                        <b>AWS S3 Product Content</b> location.
                                    </li>
                
                                    <li>
                                        The next scheduler execution will automatically
                                        process the updated Excel file and update all
                                        eligible products.
                                    </li>
                
                                </ol>
                
                                <p style="margin-bottom:0;">
                
                                    <b>Attachment:</b>
                
                                    Missing_Products_YYYY-MM-DD.csv
                
                                </p>
                
                            </div>
                
                            <br>
                
                            <p>
                
                                This is an automated email generated by the
                                <b>Jippy FoodMart Product Content</b>.
                
                                <br><br>
                
                                Please do not reply to this email.
                
                            </p>
                
                            <br>
                
                            Regards,
                
                            <br><br>
                
                            <b>Jippy FoodMart </b>
                
                        </td>
                
                    </tr>
                
                </table>
                
                </body>
                
                </html>
                """.formatted(summary.getTotalProducts(), summary.getUpdatedProducts(), summary.getMissingProducts(), summary.getFailedProducts(), summary.getTotalPages(), summary.getExecutionTimeInMillis());
    }

    private void sendEmail(String toEmail, String subject, String htmlBody, File attachment) {

        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(FmAppConstants.FROM_EMAIL);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            if (attachment != null && attachment.exists()) {

                helper.addAttachment(attachment.getName(), attachment);

                log.info("CSV attachment added successfully. File={}, Size={} bytes", attachment.getName(), attachment.length());

            } else {

                log.warn("No CSV attachment found. Sending email without attachment.");
            }

            mailSender.send(mimeMessage);

            log.info("Email sent successfully. To={}, Subject={}", toEmail, subject);

        } catch (Exception ex) {

            log.error("Failed to send email. To={}, Subject={}", toEmail, subject, ex);

            throw new EmailSendingException("Unable to send email using AWS SES.", ex);
        }
    }

    /**
     * Common Email Sender
     */

    private void sendEmail(String toEmail, String subject, String htmlBody) {

        try {

//            SendEmailRequest request = SendEmailRequest.builder()
//                    .source(fromName + " <" + fromEmail + ">")
//                    .destination(
//                            Destination.builder()
//                                    .toAddresses(toEmail)
//                                    .build()
//                    )
//                    .message(
//                            Message.builder()
//                                    .subject(
//                                            Content.builder()
//                                                    .charset("UTF-8")
//                                                    .data(subject)
//                                                    .build()
//                                    )
//                                    .body(
//                                            Body.builder()
//                                                    .html(
//                                                            Content.builder()
//                                                                    .charset("UTF-8")
//                                                                    .data(htmlBody)
//                                                                    .build()
//                                                    )
//                                                    .build()
//                                    )
//                                    .build()
//                    )
//                    .build();
//
//        sesClient.sendEmail(request);

            // 1. Create a MimeMessage instead of SimpleMailMessage
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            // 2. Use MimeMessageHelper to configure the message features
            // The boolean parameter 'true' indicates it is a multipart message (allows HTML/attachments)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(FmAppConstants.FROM_EMAIL);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            // 4. Set the text content, making sure the second parameter is set to true
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);

            log.info("Email sent successfully to {}", toEmail);

        } catch (Exception ex) {

            log.error("Failed sending email to {}", toEmail, ex);

            throw new EmailSendingException("Unable to send email using AWS SES.", ex);
        }
    }

    private String buildOtpTemplate(String otp) {

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Jippy Email Verification</title>
                </head>
                
                <body style="margin:0;padding:0;background:#F5F7F8;font-family:Arial,Helvetica,sans-serif;">
                
                <table width="100%" cellpadding="0" cellspacing="0" border="0" bgcolor="#F5F7F8">
                <tr>
                <td align="center" style="padding:30px 15px;">
                
                <table width="700" cellpadding="0" cellspacing="0" border="0"
                style="max-width:700px;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #E5E5E5;">
                
                <!-- ================= HEADER ================= -->
                
                <tr>
                <td align="center"
                style="padding:40px 30px 20px;background:#FCFFF8;">
                
                <img src="https://s3-mmrcdl1987.s3.ap-south-1.amazonaws.com/unnamed.png"
                width="220"
                alt="Jippy"
                style="display:block;border:0;">
                
                <h1 style="
                margin:25px 0 10px;
                font-size:42px;
                color:#222;
                font-weight:bold;">
                Email Verification
                </h1>
                
                <p style="
                margin:0;
                font-size:20px;
                color:#666;">
                One step closer to a better experience!
                </p>
                
                </td>
                </tr>
                
                <!-- ================= CONTENT ================= -->
                
                <tr>
                <td style="padding:45px;">
                
                <h2 style="
                margin:0 0 20px;
                font-size:36px;
                color:#3F8F00;">
                Hello,
                </h2>
                
                <p style="
                margin:0;
                font-size:22px;
                line-height:36px;
                color:#444;">
                
                Thank you for choosing <b>Jippy</b>.
                
                <br><br>
                
                Please use the OTP below to verify your email address.
                
                </p>
                
                </td>
                </tr>
                
                <!-- ================= OTP ================= -->
                
                <tr>
                <td align="center">
                
                <table
                width="560"
                cellpadding="0"
                cellspacing="0"
                style="
                width:90%;
                max-width:560px;
                border:2px solid #8BC34A;
                border-radius:14px;
                background:#FCFFF7;">
                
                <tr>
                <td align="center" style="padding:20px;">
                
                <p style="
                margin:0;
                font-size:18px;
                color:#666;
                font-weight:bold;">
                YOUR ONE TIME PASSWORD (OTP)
                </p>
                
                <div style="
                margin-top:20px;
                font-size:64px;
                font-weight:bold;
                letter-spacing:14px;
                color:#3F8F00;">
                {{OTP}}
                </div>
                
                </td>
                </tr>
                
                </table>
                
                </td>
                </tr>
                
                <!-- ================= INFO ================= -->
                
                <tr>
                
                <td style="padding:40px;">
                
                <table width="100%" cellpadding="0" cellspacing="0">
                
                <tr>
                
                <td style="
                border:1px solid #E5E5E5;
                border-radius:10px;
                padding:22px;">
                
                <h3 style="
                margin:0 0 10px;
                color:#3F8F00;
                font-size:24px;">
                OTP Validity
                </h3>
                
                <p style="
                margin:0;
                font-size:18px;
                line-height:30px;
                color:#444;">
                This OTP is valid for
                <b style="color:#3F8F00;">10 minutes</b>
                only.
                </p>
                
                </td>
                
                </tr>
                
                <tr><td height="20"></td></tr>
                
                <tr>
                
                <td style="
                border:1px solid #E5E5E5;
                border-radius:10px;
                padding:22px;">
                
                <h3 style="
                margin:0 0 10px;
                color:#3F8F00;
                font-size:24px;">
                Security
                </h3>
                
                <p style="
                margin:0;
                font-size:18px;
                line-height:30px;
                color:#444;">
                
                Never share your OTP with anyone.
                
                <br>
                
                Jippy will never ask for your OTP.
                
                </p>
                
                </td>
                
                </tr>
                
                <tr><td height="20"></td></tr>
                
                <tr>
                
                <td style="
                background:#FFF8E7;
                border:1px solid #F3C65A;
                border-radius:10px;
                padding:20px;">
                
                <p style="
                margin:0;
                font-size:18px;
                line-height:30px;
                color:#444;">
                
                <b>If you didn't request this verification,</b>
                
                you can safely ignore this email.
                
                </p>
                
                </td>
                
                </tr>
                
                </table>
                
                </td>
                
                </tr>
                
                <!-- ================= FOOTER ================= -->
                
                <tr>
                
                <td align="center"
                style="padding:35px;">
                
                <p style="
                margin:0;
                font-size:22px;
                color:#444;">
                Regards,
                </p>
                
                <h2 style="
                margin:15px 0 0;
                color:#3F8F00;
                font-size:34px;">
                Team Jippy
                </h2>
                
                </td>
                
                </tr>
                
                <tr>
                
                <td align="center"
                style="
                background:#3F8F00;
                padding:22px;
                color:#fff;
                font-size:16px;
                line-height:28px;">
                
                © 2026 Jippy Technologies Pvt. Ltd.
                
                <br>
                
                All Rights Reserved.
                
                </td>
                
                </tr>
                
                </table>
                
                </td>
                
                </tr>
                
                </table>
                
                </body>
                </html>
                """;

        return html.replace("{{OTP}}", otp);
    }
}