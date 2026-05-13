package com.jippy.foodandmart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class FmPasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt automatically handles "salting" to prevent rainbow table attacks
        return new BCryptPasswordEncoder();
    }
}
