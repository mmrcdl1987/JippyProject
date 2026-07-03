package com.jippy.foodandmart.security;
import com.jippy.foodandmart.Enum.FmOtpPurpose;
import com.jippy.foodandmart.entity.FmUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private static final long ACCESS_TOKEN_VALIDITY = 24 * 60 * 60 * 1000L; // 24 Hours
    private static final long OTP_TOKEN_VALIDITY = 15 * 60 * 1000L;
    // 15 Minutes
    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication auth) {
        FmUser user = (FmUser) auth.getPrincipal();
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", roles)
                .claim("userId", user.getUserId()) // Important for your microservices
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY)) // 24 hours
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                //                .signWith(getSigningKey())
                .compact();
    }


    // KEEP: Useful if you want to extract the username inside the microservice
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // NEW: Useful to get roles or userId directly from the JWT if needed
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    /**
     * Signup / OTP Verified JWT
     */
    public String generateSignupToken(String email, FmOtpPurpose purpose) {

        return Jwts.builder()
                .subject(email)
                .claim("purpose", purpose.name())
                .claim("verified", true)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + OTP_TOKEN_VALIDITY))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate JWT
     */
    public boolean isTokenValid(String token) {

        try {

            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

}
