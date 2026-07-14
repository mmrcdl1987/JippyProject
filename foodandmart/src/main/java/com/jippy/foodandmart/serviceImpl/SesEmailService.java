package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.exception.EmailSendingException;
import com.jippy.foodandmart.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class SesEmailService implements EmailService {

    private final SesClient sesClient;

    @Value("${aws.ses.from-email}")
    private String fromEmail;

    @Value("${aws.ses.from-name}")
    private String fromName;

    @Value("${application.name:Jippy}")
    private String applicationName;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        sendEmail(
                toEmail,
                applicationName + " - Email Verification OTP",
                buildOtpTemplate(otp)
        );
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String merchantName) {
        sendEmail(
                toEmail,
                "Welcome to " + applicationName,
                buildWelcomeTemplate(merchantName)
        );
    }

    @Override
    public void sendForgotPasswordOtp(String toEmail, String otp) {
        sendEmail(
                toEmail,
                applicationName + " - Password Reset OTP",
                buildForgotPasswordTemplate(otp)
        );
    }

    /**
     * Common Email Sender
     */
    private void sendEmail(String toEmail, String subject, String htmlBody) {

        try {

            SendEmailRequest request = SendEmailRequest.builder()
                    .source(fromName + " <" + fromEmail + ">")
                    .destination(
                            Destination.builder()
                                    .toAddresses(toEmail)
                                    .build()
                    )
                    .message(
                            Message.builder()
                                    .subject(
                                            Content.builder()
                                                    .charset("UTF-8")
                                                    .data(subject)
                                                    .build()
                                    )
                                    .body(
                                            Body.builder()
                                                    .html(
                                                            Content.builder()
                                                                    .charset("UTF-8")
                                                                    .data(htmlBody)
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            sesClient.sendEmail(request);

            log.info("Email sent successfully to {}", toEmail);

        } catch (Exception ex) {

            log.error("Failed sending email to {}", toEmail, ex);

            throw new EmailSendingException(
                    "Unable to send email using AWS SES.",
                    ex
            );
        }
    }

    /**
     * OTP Email
     */
    private String buildOtpTemplate(String otp) {

        return """
                <html>
                <body style="font-family:Arial,sans-serif;padding:20px">

                    <h2>Jippy Email Verification</h2>

                    <p>Your One Time Password is</p>

                    <h1 style="color:#0A84FF">%s</h1>

                    <p>
                        This OTP is valid for
                        <b>10 minutes</b>.
                    </p>

                    <p>
                        Please do not share this OTP with anyone.
                    </p>

                    <br>

                    <b>Team Jippy</b>

                </body>
                </html>
                """.formatted(otp);
    }

    /**
     * Welcome Email
     */
    private String buildWelcomeTemplate(String merchantName) {

        return """
                <html>
                <body style="font-family:Arial,sans-serif;padding:20px">

                    <h2>Welcome %s</h2>

                    <p>
                        Your merchant account has been created successfully.
                    </p>

                    <p>
                        Thank you for choosing Jippy.
                    </p>

                    <br>

                    <b>Team Jippy</b>

                </body>
                </html>
                """.formatted(merchantName);
    }

    /**
     * Forgot Password Email
     */
    private String buildForgotPasswordTemplate(String otp) {

        return """
                <html>
                <body style="font-family:Arial,sans-serif;padding:20px">

                    <h2>Password Reset</h2>

                    <p>Your OTP is</p>

                    <h1 style="color:red">%s</h1>

                    <p>
                        OTP expires in <b>10 minutes</b>.
                    </p>

                    <p>
                        Please do not share this OTP with anyone.
                    </p>

                    <br>

                    <b>Team Jippy</b>

                </body>
                </html>
                """.formatted(otp);
    }
}