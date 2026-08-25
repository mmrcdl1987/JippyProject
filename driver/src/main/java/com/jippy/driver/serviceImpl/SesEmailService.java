package com.jippy.driver.serviceImpl;

import com.jippy.driver.constants.DConstants;
import com.jippy.driver.exception.EmailSendingException;
import com.jippy.driver.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SesEmailService implements EmailService {


    // private final SesClient sesClient;
    private final JavaMailSender mailSender;

    @Override
    public void sendDriverRegistrationEmail(String driverEmail, String driverName) {

        log.info("DRIVER_REGISTRATION_EMAIL_START | email={}, driverName={}", driverEmail, driverName);

        String subject = "Welcome to Jippy Food Delivery - Driver Registration Successful";

        String htmlBody = buildDriverRegistrationTemplate(driverName, driverEmail);

        sendEmail(driverEmail, subject, htmlBody);

        log.info("DRIVER_REGISTRATION_EMAIL_SUCCESS | email={}, driverName={}", driverEmail, driverName);
    }

    @Override
    public void sendDriverApprovedEmail(String driverEmail, String driverName) {

        log.info("DRIVER_APPROVED_EMAIL_START | email={}, driverName={}", driverEmail, driverName);

        String subject = "Congratulations! Your Driver Account is Approved - Jippy Food Delivery";

        String htmlBody = buildDriverApprovedTemplate(driverName, driverEmail);

        sendEmail(driverEmail, subject, htmlBody);

        log.info("DRIVER_APPROVED_EMAIL_SUCCESS | email={}, driverName={}", driverEmail, driverName);
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

            helper.setFrom(DConstants.FROM_EMAIL);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            // 4. Set the text content, making sure the second parameter is set to true
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);

            log.info("Email sent successfully to {}", toEmail);

        } catch (Exception ex) {

            log.error("Failed sending email to {}", toEmail, ex);

            throw new EmailSendingException("Unable to send email using AWS SES.");
        }
    }

    private String buildDriverRegistrationTemplate(String driverName, String driverEmail) {

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                
                    <title>Driver Registration - Jippy Food Delivery</title>
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
                
                                <!-- HEADER -->
                
                                <tr>
                                    <td style="
                                        padding:22px 30px;
                                        border-bottom:1px solid #EEEEEE;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0">
                
                                            <tr>
                
                                                <td align="left">
                
                                                    <div style="
                                                        font-size:30px;
                                                        font-weight:bold;
                                                        color:#20242A;
                                                    ">
                                                        🛵
                                                        <span style="color:#20242A;">
                                                            Jippy
                                                        </span>
                                                        <span style="color:#F36F21;">
                                                            Food Delivery
                                                        </span>
                                                    </div>
                
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
                
                
                                <!-- HERO -->
                
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
                                                    valign="middle">
                
                                                    <div style="
                                                        font-size:36px;
                                                        line-height:44px;
                                                        font-weight:bold;
                                                        color:#222222;
                                                    ">
                                                        Welcome to
                                                    </div>
                
                                                    <div style="
                                                        font-size:40px;
                                                        line-height:48px;
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
                                                        Your driver registration
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
                                                        font-size:85px;
                                                    ">
                                                        🛵
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
                
                
                                <!-- GREETING -->
                
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
                
                                            Thank you for registering as a
                                            delivery partner with
                                            <b>Jippy Food Delivery</b>.
                
                                        </p>
                
                                        <p style="
                                            margin:12px 0 0;
                                            font-size:17px;
                                            line-height:29px;
                                            color:#444444;
                                        ">
                
                                            We have successfully received your
                                            driver registration details. Our team
                                            will review your information before
                                            approving your driver account.
                
                                        </p>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- DRIVER DETAILS -->
                
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
                
                                                    🛵 &nbsp; DRIVER DETAILS
                
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
                                                                Driver Name
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
                
                
                                <!-- NEXT STEPS -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
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
                                                        📋 &nbsp; WHAT'S NEXT?
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
                                                        your driver details.
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
                                                        &nbsp; Complete any required
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
                                                        &nbsp; Wait for your driver
                                                        account approval.
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
                                                        &nbsp; After approval, start
                                                        accepting delivery orders.
                                                    </p>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- DRIVER PARTNER -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               style="
                                                   background:#FFF8E7;
                                                   border:1px solid #F2D58B;
                                                   border-radius:10px;
                                               ">
                
                                            <tr>
                
                                                <td style="padding:22px;">
                
                                                    <div style="
                                                        font-size:21px;
                                                        font-weight:bold;
                                                        color:#D96B00;
                                                    ">
                                                        🤝 Driver Partner
                                                    </div>
                
                                                    <p style="
                                                        margin:8px 0 0;
                                                        font-size:15px;
                                                        line-height:25px;
                                                        color:#555555;
                                                    ">
                
                                                        Once your account is approved,
                                                        you can start accepting
                                                        delivery assignments and
                                                        serve customers through
                                                        Jippy Food Delivery.
                
                                                    </p>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- SUPPORT -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 35px 30px;
                                    ">
                
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
                                                        font-size:21px;
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
                
                
                                <!-- FOOTER -->
                
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
                
                                            🛵 Jippy
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
                """.formatted(driverName, driverName, driverEmail);
    }

    private String buildDriverApprovedTemplate(String driverName, String driverEmail) {

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                
                    <title>Driver Account Approved - Jippy Food Delivery</title>
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
                            style="padding:25px 10px;">
                
                            <table width="720"
                                   cellpadding="0"
                                   cellspacing="0"
                                   border="0"
                                   style="
                                       width:100%%;
                                       max-width:720px;
                                       background:#FFFFFF;
                                       border:1px solid #E5E5E5;
                                       border-radius:14px;
                                       overflow:hidden;
                                   ">
                
                                <!-- HEADER -->
                
                                <tr>
                
                                    <td style="
                                        padding:22px 30px;
                                        border-bottom:1px solid #EEEEEE;
                                    ">
                
                                        <table width="100%%">
                
                                            <tr>
                
                                                <td>
                
                                                    <div style="
                                                        font-size:30px;
                                                        font-weight:bold;
                                                        color:#20242A;
                                                    ">
                                                        🛵 Jippy
                                                        <span style="
                                                            color:#F36F21;
                                                        ">
                                                            Food Delivery
                                                        </span>
                                                    </div>
                
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
                
                
                                <!-- HERO -->
                
                                <tr>
                
                                    <td style="
                                        padding:45px 35px;
                                        background:#EEF7FF;
                                    ">
                
                                        <table width="100%%">
                
                                            <tr>
                
                                                <td width="55%%"
                                                    valign="middle">
                
                                                    <div style="
                                                        font-size:36px;
                                                        line-height:44px;
                                                        font-weight:bold;
                                                        color:#222222;
                                                    ">
                                                        Congratulations!
                                                    </div>
                
                                                    <div style="
                                                        font-size:40px;
                                                        line-height:48px;
                                                        font-weight:bold;
                                                        color:#F36F21;
                                                    ">
                                                        You're Approved!
                                                    </div>
                
                                                    <div style="
                                                        width:65px;
                                                        height:5px;
                                                        background:#2E9B50;
                                                        margin:18px 0;
                                                        border-radius:5px;
                                                    "></div>
                
                                                    <p style="
                                                        margin:0;
                                                        font-size:17px;
                                                        line-height:28px;
                                                        color:#333333;
                                                    ">
                
                                                        Your driver account has been
                                                        successfully approved.
                
                                                    </p>
                
                                                </td>
                
                
                                                <td width="45%%"
                                                    align="center">
                
                                                    <div style="
                                                        font-size:90px;
                                                    ">
                                                        🛵
                                                    </div>
                
                                                    <div style="
                                                        display:inline-block;
                                                        padding:10px 20px;
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
                
                
                                <!-- GREETING -->
                
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
                
                                            Great news! Your driver account has
                                            been approved by
                                            <b>Jippy Food Delivery</b>.
                
                                        </p>
                
                                        <p style="
                                            margin:12px 0 0;
                                            font-size:17px;
                                            line-height:29px;
                                            color:#444444;
                                        ">
                
                                            You are now ready to start your journey
                                            as a Jippy delivery partner.
                
                                        </p>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- DRIVER DETAILS -->
                
                                <tr>
                
                                    <td style="
                                        padding:20px 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               style="
                                                   border:1px solid #DCDCDC;
                                                   border-radius:12px;
                                               ">
                
                                            <tr>
                
                                                <td style="
                                                    padding:20px;
                                                ">
                
                                                    <div style="
                                                        font-size:21px;
                                                        font-weight:bold;
                                                        color:#F36F21;
                                                        margin-bottom:15px;
                                                    ">
                                                        🛵 &nbsp; DRIVER DETAILS
                                                    </div>
                
                                                    <table width="100%%">
                
                                                        <tr>
                
                                                            <td width="42%%"
                                                                style="
                                                                    padding:9px 0;
                                                                    color:#666666;
                                                                    font-weight:bold;
                                                                ">
                                                                Driver Name
                                                            </td>
                
                                                            <td style="
                                                                padding:9px 0;
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
                                                                Driver Email
                                                            </td>
                
                                                            <td style="
                                                                padding:9px 0;
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
                                                                color:#2E9B50;
                                                                font-weight:bold;
                                                            ">
                                                                ONLINE / APPROVED
                                                            </td>
                
                                                        </tr>
                
                                                    </table>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- NEXT STEPS -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               style="
                                                   background:#F5FFF7;
                                                   border:1px solid #D6EEDB;
                                                   border-radius:12px;
                                               ">
                
                                            <tr>
                
                                                <td style="
                                                    padding:25px;
                                                ">
                
                                                    <div style="
                                                        font-size:22px;
                                                        font-weight:bold;
                                                        color:#2E9B50;
                                                        margin-bottom:15px;
                                                    ">
                                                        🎯 &nbsp; NEXT STEPS
                                                    </div>
                
                                                    <p style="
                                                        margin:10px 0;
                                                        font-size:16px;
                                                        line-height:27px;
                                                    ">
                                                        ✓ Complete your driver profile
                                                    </p>
                
                                                    <p style="
                                                        margin:10px 0;
                                                        font-size:16px;
                                                        line-height:27px;
                                                    ">
                                                        ✓ Keep your documents and
                                                        vehicle details updated
                                                    </p>
                
                                                    <p style="
                                                        margin:10px 0;
                                                        font-size:16px;
                                                        line-height:27px;
                                                    ">
                                                        ✓ Stay available to accept
                                                        delivery assignments
                                                    </p>
                
                                                    <p style="
                                                        margin:10px 0;
                                                        font-size:16px;
                                                        line-height:27px;
                                                    ">
                                                        ✓ Deliver orders safely and
                                                        professionally
                                                    </p>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- DRIVER RESPONSIBILITIES -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 35px 30px;
                                    ">
                
                                        <table width="100%%"
                                               style="
                                                   background:#FFF8E7;
                                                   border:1px solid #F2D58B;
                                                   border-radius:10px;
                                               ">
                
                                            <tr>
                
                                                <td style="
                                                    padding:22px;
                                                ">
                
                                                    <div style="
                                                        font-size:21px;
                                                        font-weight:bold;
                                                        color:#D96B00;
                                                    ">
                                                        🛡️ Driver Responsibilities
                                                    </div>
                
                                                    <p style="
                                                        margin:8px 0;
                                                        font-size:15px;
                                                        line-height:25px;
                                                        color:#555555;
                                                    ">
                                                        • Accept orders responsibly.
                                                    </p>
                
                                                    <p style="
                                                        margin:8px 0;
                                                        font-size:15px;
                                                        line-height:25px;
                                                        color:#555555;
                                                    ">
                                                        • Handle customer orders
                                                          carefully.
                                                    </p>
                
                                                    <p style="
                                                        margin:8px 0;
                                                        font-size:15px;
                                                        line-height:25px;
                                                        color:#555555;
                                                    ">
                                                        • Follow traffic and safety
                                                          rules.
                                                    </p>
                
                                                    <p style="
                                                        margin:8px 0;
                                                        font-size:15px;
                                                        line-height:25px;
                                                        color:#555555;
                                                    ">
                                                        • Maintain professional
                                                          customer service.
                                                    </p>
                
                                                </td>
                
                                            </tr>
                
                                        </table>
                
                                    </td>
                
                                </tr>
                
                
                                <!-- SUPPORT -->
                
                                <tr>
                
                                    <td style="
                                        padding:0 35px 30px;
                                    ">
                
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
                                                        font-size:21px;
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
                
                
                                <!-- FOOTER -->
                
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
                
                                            🛵 Jippy
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
                """.formatted(driverName, driverName, driverEmail);
    }

}
