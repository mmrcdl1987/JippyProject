package com.jippy.driver.controller;

import com.jippy.driver.serviceImpl.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @GetMapping("/test")
    public String sendTestMail() {

        emailService.sendTestMail();

        return "Mail Sent Successfully";
    }
}