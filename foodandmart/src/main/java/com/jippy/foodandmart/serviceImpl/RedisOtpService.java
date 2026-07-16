
package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisOtpService {

    private static final String OTP_PREFIX = "OTP:";

    private final StringRedisTemplate redisTemplate;

//    @Value("${otp.expiry-minutes}")
//    private long otpExpiryMinutes;


/**
     * Store BCrypt OTP hash in Redis.
     */

    public void saveOtpHash(String key, String otpHash) {

        redisTemplate.opsForValue().set(
                OTP_PREFIX + key,
                otpHash,
                Duration.ofMinutes(FmAppConstants.EMAIL_OTP_EXPIRY_MINUTES)
        );

        log.info("OTP hash stored in Redis. Key={}, Expiry={} minutes",
                OTP_PREFIX + key, FmAppConstants.EMAIL_OTP_EXPIRY_MINUTES);
    }

/**
     * Retrieve BCrypt OTP hash.
     */

    public String getOtpHash(String key) {
        return redisTemplate.opsForValue().get(OTP_PREFIX + key);
    }


/**
     * Check whether OTP exists.
     */

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(OTP_PREFIX + key));
    }


/**
     * Delete OTP.
     */

    public void deleteOtp(String key) {

        redisTemplate.delete(OTP_PREFIX + key);

        log.info("OTP removed from Redis. Key={}", OTP_PREFIX + key);
    }


/**
     * Remaining TTL (seconds).
     */

    public Long getRemainingTime(String key) {
        return redisTemplate.getExpire(OTP_PREFIX + key);
    }
}
