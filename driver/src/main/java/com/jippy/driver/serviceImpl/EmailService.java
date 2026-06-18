package com.jippy.driver.serviceImpl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @SneakyThrows
    public void sendTestMail() {

        log.info("Preparing test mail");

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        File folder = new File("C:\\Users\\rohan\\Downloads\\Jippy Mart Mail Documents");

        File pdfFile = new File(folder, "invoice.pdf");
        File excelFile = new File(folder, "report.xlsx");
        File wordFile = new File(folder, "restaurant-agreement.docx");
        File image1 = new File(folder, "Jippy1.jpg");
        File text = new File(folder, "delivery-notes.txt");


        helper.setFrom("jippyfooddelivery2026@gmail.com");
        helper.setTo("rohanvadluri8463@gmail.com");
        helper.addCc("yelisettymaniteja@gmail.com");
        helper.addCc("mmrcdl1987@gmail.com");
        helper.addCc("mohan@jippymart.in");
        helper.addCc("sudheer@jippymart.in");
        helper.addCc("munjachandana@gmail.com");
        helper.addCc("mahendra05062001@gmail.com");
        helper.addCc("rohanvadluri@gmail.com");
//        for multiple Cc's
//        helper.setCc(new String[]{
//                "munjachandana@gmail.com",
//                "rohanvadluri@gmail.com",
//                "srk.bench3@gmail.com"
//
//        });
        helper.setSubject("Testing all Attachements For Mail integration");
        helper.setText("Please find the attached Documents");

        String htmlContent = """
                <html>
                    <body>
                        <h2>Welcome to Jippy Mart Food Delivery App</h2>
                        <p> Thank you for choosing Jippy Mart! We are delighted to have you as part of our growing community.
                                         Jippy Mart is a convenient food delivery platform that connects customers with their favorite restaurants
                                          and delivers delicious meals right to their doorstep.</p>
                    </body>
                </html>
                """;

//        file types
        helper.setText(htmlContent, true);
        // PDF
        helper.addAttachment("invoice.pdf", pdfFile);

        // Excel
        helper.addAttachment("report.xlsx", excelFile);

        // Word
        helper.addAttachment("restaurant-agreement.docx", wordFile);
        log.info("Image1 Exists: {}", image1.exists());
        log.info("Image1 Path: {}", image1.getAbsolutePath());

        // Image
        helper.addAttachment(image1.getName(), image1);

        // Text File
        helper.addAttachment("delivery-notes.txt", text);
        mailSender.send(mimeMessage);

//      this class is used to create a simple email message with basic properties such as
//         sender, recipient, subject, and body text.
//         SimpleMailMessage message = new SimpleMailMessage();

//        message.setFrom("jippyfooddelivery2026@gmail.com");
//        message.setTo("rohanvadluri8463@gmail.com");
//        message.setCc("munjachandana@gmail.com");
//        message.setCc("yelisettymaniteja@gmail.com");
//        message.setCc("mmrcdl1987@gmail.com");
//        message.setBcc("sudheer@jippymart.in");

//
//        message.setSubject("Jippy Driver Service Test Mail");
//
//        message.setText(
//                """
//                        Hello This Is Rohan,
//
//                        This is my first email Integration from Spring Boot Driver Service.
//
//                        Regards,
//                        Jippy Team""");
//
//        mailSender.send(message);


        log.info("Mail sent successfully");
    }
}