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