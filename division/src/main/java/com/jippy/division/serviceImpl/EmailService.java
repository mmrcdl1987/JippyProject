package com.jippy.division.serviceImpl;

import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.entity.DivOutletWeeklySettlement;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateService templateService;


    public void sendTestMail() {

        SimpleMailMessage message = new SimpleMailMessage();

//         jippy default example mail
        message.setFrom("jippyfooddelivery2026@gmail.com");
        message.setTo("munjachandana@gmail.com");
        message.setCc("rohanvadluri8463@gmail.com");
        message.setCc("yelisettymaniteja@gmail.com");
        message.setCc("mmrcdl1987@gmail.com");

        message.setSubject("Division Service Test");

        message.setText("Testing mail from Division Service From" +
                " Resources/MerchantSettlements.html Template");

        mailSender.send(message);
    }

    @SneakyThrows
    public void sendSettlementMail(DivOutletWeeklySettlement settlement) {

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setFrom(DivAppConstants.JIPPY_DEFAULT_EMAIL);
        helper.setTo(settlement.getOutletEmail());
        helper.setCc("thatikondaprathyusha56@gmail.com");

        helper.setSubject(DivAppConstants.EMAIL_SUBJECT);


//         calling template service --loadTemplate function to get Html template
//         Load Template
        String htmlContent = templateService.loadTemplate("merchant-settlement.html");

//       Replace Placeholders
        htmlContent = htmlContent.replace("{{weeklySettlementId}}",
                String.valueOf(settlement.getWeeklySettlementId()));

        htmlContent = htmlContent.replace("{{outletId}}", String.valueOf(settlement.getOutletId()));

        htmlContent = htmlContent.replace("{{weekStartDate}}", String.valueOf(settlement.getWeekStartDate()));

        htmlContent = htmlContent.replace("{{weekEndDate}}", String.valueOf(settlement.getWeekEndDate()));

        htmlContent = htmlContent.replace("{{ordersCount}}", String.valueOf(settlement.getOrdersCount()));

        htmlContent = htmlContent.replace("{{totalSettlementAmount}}", String.valueOf(settlement.getTotalSettlementAmount()));

        htmlContent = htmlContent.replace("{{deductions}}", String.valueOf(settlement.getDeductions()));

        htmlContent = htmlContent.replace("{{gst}}", String.valueOf(settlement.getGst()));

        htmlContent = htmlContent.replace("{{promotionAmount}}", String.valueOf(settlement.getPromotionAmount()));

        htmlContent = htmlContent.replace("{{subscriptionAmount}}", String.valueOf(settlement.getSubscriptionAmount()));

        htmlContent = htmlContent.replace("{{netSettlementAmount}}", String.valueOf(settlement.getNetSettlementAmount()));

        htmlContent = htmlContent.replace("{{paymentStatus}}", String.valueOf(settlement.getPaymentStatus()));

        htmlContent = htmlContent.replace("{{transactionId}}", String.valueOf(settlement.getTransactionId()));


//
        System.out.println(
                new ClassPathResource(
                        "static/images/JippyMartBannerTemplate.png"
                ).exists()
        );

        helper.setText(htmlContent, true);

        //Add Banner Image
        ClassPathResource bannerImage = new ClassPathResource("static/images/JippyMartBannerTemplate.png");
        helper.addInline("bannerImage", bannerImage);
        System.out.println(bannerImage.exists());

        mailSender.send(mimeMessage);

        log.info("Settlement mail sent to : {}", settlement.getOutletEmail());
    }

//     for api in Fm to send otp to email's
    public void sendOtpMail(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(DivAppConstants.JIPPY_DEFAULT_EMAIL);

        message.setTo(email);
//        message.setCc("srk.bench3@gmail.com");
//        message.setCc("thatikondaprathyusha56@gmail.com");

        message.setSubject("Jippy Food Delivery Password Reset OTP");

        message.setText(
                "Dear User,\n\n" +
                        "Your OTP for password reset is : " + otp +
                        "\n\nOTP is valid for 5 minutes." +
                        "\n\nRegards,\nJippy Team"
        );

        mailSender.send(message);

        log.info("OTP mail sent successfully to {}", email);
    }
}
