package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.exception.JwtGenerationException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(CoCustomer customer) {

        if (customer == null) {
            throw new JwtGenerationException("Customer cannot be null");
        }

        if (customer.getCustomerId() == null) {
            throw new JwtGenerationException("Customer ID cannot be null");
        }

        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().isBlank()) {

            throw new JwtGenerationException("Customer mobile number cannot be null");
        }

        if (secret == null || secret.isBlank()) {
            throw new JwtGenerationException("JWT secret is not configured");
        }

        if (expiration == null || expiration <= 0) {
            throw new JwtGenerationException("JWT expiration is invalid");
        }

        try {

            String jti = UUID.randomUUID().toString();

            Date issuedAt = new Date();

            Date expiresAt = new Date(System.currentTimeMillis() + expiration);

            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            String token = Jwts.builder().id(jti).subject(customer.getCustomerId().toString()).claim("customerId", customer.getCustomerId()).claim("mobile", customer.getPhoneNumber()).claim("role", "CUSTOMER").issuedAt(issuedAt).expiration(expiresAt).signWith(key).compact();

            log.info("JWT_SERVICE | GENERATE_TOKEN | customerId={} | jti={} | SUCCESS", customer.getCustomerId(), jti);

            return token;

        } catch (Exception ex) {

            log.error("JWT_SERVICE | GENERATE_TOKEN | customerId={} | ERROR={}", customer.getCustomerId(), ex.getMessage(), ex);

            throw new JwtGenerationException("Failed to generate JWT token");
        }
    }
}