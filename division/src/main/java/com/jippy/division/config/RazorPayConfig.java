package com.jippy.division.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class RazorPayConfig {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        // This MUST print on startup if configured right
        System.out.println("=========================================");
        System.out.println("LOADING RAZORPAY KEY: " + keyId);
        System.out.println("=========================================");
        return new RazorpayClient(keyId, keySecret);
    }
}
