package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmMerchant;
import com.jippy.foodandmart.entity.FmUserOtp;
import com.jippy.foodandmart.enums.FmOtpType;
import com.jippy.foodandmart.enums.FmOtpUserType;
import com.jippy.foodandmart.exception.InvalidOtpException;
import com.jippy.foodandmart.exception.OtpExpiredException;
import com.jippy.foodandmart.exception.UserNotFoundException;
import com.jippy.foodandmart.feignClients.DriverFeignClient;
import com.jippy.foodandmart.repository.FmMerchantRepository;
import com.jippy.foodandmart.repository.FmUserOtpRepository;
import com.jippy.foodandmart.security.JwtUtils;
import com.jippy.foodandmart.service.OtpLoginService;
import com.jippy.foodandmart.service.SmsCountryService;
import com.jippy.foodandmart.util.OtpGenerator;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpLoginServiceImpl implements OtpLoginService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_RESEND_COUNT = 3;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final FmMerchantRepository fmMerchantRepository;
    private final FmUserOtpRepository fmUserOtpRepository;
    private final DriverFeignClient driverFeignClient;
    private final SmsCountryService smsCountryService;
    private final PasswordEncoder passwordEncoder;
    private final OtpGenerator otpGenerator;
    private final JwtUtils jwtUtils;

    @Override
    public SendOtpResponseDto sendLoginOtp(SendLoginOtpRequestDto requestDto) {

        log.info("OTP_LOGIN | SEND_OTP | userType={} | mobile={} | START", requestDto.getUserType(), requestDto.getMobileNumber());

        Long userId = getUserId(requestDto.getUserType(), requestDto.getMobileNumber());

        String otp = otpGenerator.generateOtp();

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        FmUserOtp userOtp = fmUserOtpRepository
                .findTopByUserIdAndUserTypeAndOtpTypeOrderByCreatedAtDesc(
                        userId,
                        requestDto.getUserType().name(),
                        FmOtpType.LOGIN.name()
                )
                .orElseGet(FmUserOtp::new);

        userOtp.setUserId(userId);
        userOtp.setUserType(requestDto.getUserType().name());
        userOtp.setPhoneNumber(requestDto.getMobileNumber());
        userOtp.setOtpType(FmOtpType.LOGIN.name());

        userOtp.setOtpHash(passwordEncoder.encode(otp));
        userOtp.setAttemptCount(0);
        userOtp.setResendCount(0);
        userOtp.setVerified(false);
        userOtp.setVerifiedAt(null);
        userOtp.setUsed(false);
        userOtp.setUsedAt(null);
        userOtp.setLastAttemptAt(null);
        userOtp.setExpiresAt(expiresAt);

        fmUserOtpRepository.save(userOtp);
        smsCountryService.sendOtp(requestDto.getMobileNumber(), otp);

        log.info("OTP_LOGIN | SEND_OTP | userType={} | userId={} | SUCCESS", requestDto.getUserType(), userId);

        return SendOtpResponseDto.builder()
                .message("OTP sent successfully")
                .expiresInMinutes((long) OTP_EXPIRY_MINUTES)
                .build();
    }

    @Override
    public SendOtpResponseDto resendLoginOtp(SendLoginOtpRequestDto requestDto) {

        log.info("OTP_LOGIN | RESEND_OTP | userType={} | mobile={} | START", requestDto.getUserType(), requestDto.getMobileNumber());

        Long userId = getUserId(requestDto.getUserType(), requestDto.getMobileNumber());

        FmUserOtp userOtp = fmUserOtpRepository
                .findTopByUserIdAndUserTypeAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(
                        userId,
                        requestDto.getUserType().name(),
                        FmOtpType.LOGIN.name()
                )
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "No active OTP found. Please request a new OTP."
                        )
                );
        if (userOtp.getResendCount() >= MAX_RESEND_COUNT) {

            throw new IllegalArgumentException("Maximum OTP resend limit reached. Please request a new OTP later.");
        }

        String otp = otpGenerator.generateOtp();

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        userOtp.setOtpHash(passwordEncoder.encode(otp));

        userOtp.setExpiresAt(expiresAt);

        userOtp.setResendCount(userOtp.getResendCount() + 1);

        userOtp.setAttemptCount(0);
        userOtp.setVerified(false);
        userOtp.setVerifiedAt(null);
        userOtp.setUsed(false);
        userOtp.setUsedAt(null);
        userOtp.setLastAttemptAt(null);

        fmUserOtpRepository.save(userOtp);

        smsCountryService.sendOtp(requestDto.getMobileNumber(), otp);

        log.info("OTP_LOGIN | RESEND_OTP | userType={} | userId={} | resendCount={} | SUCCESS", requestDto.getUserType(), userId, userOtp.getResendCount());

        return SendOtpResponseDto.builder()
                .message("OTP resent successfully")
                .expiresInMinutes((long) OTP_EXPIRY_MINUTES)
                .build();
    }

    @Override
    public AuthResponseDto verifyLoginOtp(VerifyLoginOtpRequestDto requestDto) {

        log.info("OTP_LOGIN | VERIFY_OTP | userType={} | mobile={} | START", requestDto.getUserType(), requestDto.getMobileNumber());

        Long userId = getUserId(requestDto.getUserType(), requestDto.getMobileNumber());

        FmUserOtp userOtp = fmUserOtpRepository
                .findTopByUserIdAndUserTypeAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(
                        userId,
                        requestDto.getUserType().name(),
                        FmOtpType.LOGIN.name()
                )
                .orElseThrow(() ->
                        new InvalidOtpException(
                                "OTP not found. Please request a new OTP."
                        )
                );
        if (LocalDateTime.now().isAfter(userOtp.getExpiresAt())) {

            throw new OtpExpiredException("OTP has expired. Please request a new OTP.");
        }

        if (userOtp.getAttemptCount() >= MAX_OTP_ATTEMPTS) {

            throw new InvalidOtpException("Maximum OTP verification attempts exceeded. Please request a new OTP.");
        }

        boolean otpMatched = passwordEncoder.matches(requestDto.getOtp(), userOtp.getOtpHash());

        if (!otpMatched) {

            userOtp.setAttemptCount(userOtp.getAttemptCount() + 1);

            userOtp.setLastAttemptAt(LocalDateTime.now());

            fmUserOtpRepository.save(userOtp);

            log.warn("OTP_LOGIN | VERIFY_OTP | INVALID | userType={} | userId={} | attempts={}", requestDto.getUserType(), userId, userOtp.getAttemptCount());

            throw new InvalidOtpException("Invalid OTP");
        }

        LocalDateTime now = LocalDateTime.now();

        userOtp.setVerified(true);
        userOtp.setVerifiedAt(now);

        userOtp.setUsed(true);
        userOtp.setUsedAt(now);

        userOtp.setLastAttemptAt(now);

        fmUserOtpRepository.save(userOtp);

        List<String> roles = getRoles(requestDto.getUserType());

        String jwt = jwtUtils.generateOtpLoginToken(userId, requestDto.getMobileNumber(), requestDto.getUserType().name(), roles);

        AuthResponseDto responseDto = new AuthResponseDto();

        responseDto.setJwt(jwt);
        responseDto.setUserType(requestDto.getUserType().name());
        responseDto.setUserId(userId.intValue());
        responseDto.setRoles(roles);

        log.info("OTP_LOGIN | VERIFY_OTP | SUCCESS | userType={} | userId={}", requestDto.getUserType(), userId);

        return responseDto;
    }

    private List<String> getRoles(FmOtpUserType userType) {

        return switch (userType) {

            case MERCHANT -> List.of("ROLE_MERCHANT");

            case DRIVER -> List.of("ROLE_DRIVER");
        };
    }

    private Long getUserId(FmOtpUserType userType, String mobileNumber) {

        return switch (userType) {

            case MERCHANT -> getMerchantId(mobileNumber);

            case DRIVER -> getDriverId(mobileNumber);
        };
    }

    private Long getMerchantId(String mobileNumber) {

        FmMerchant merchant = fmMerchantRepository.findByMerchantPhone(mobileNumber).orElseThrow(() -> new UserNotFoundException("Merchant not found with this mobile number"));

        return merchant.getMerchantId().longValue();
    }

    private Long getDriverId(String mobileNumber) {

        try {

            DriverDto driver = driverFeignClient.findByPhoneNumber(mobileNumber);

            if (driver == null || driver.getDriverId() == null) {
                throw new UserNotFoundException("Driver not found with this mobile number");
            }

            return driver.getDriverId().longValue();

        } catch (FeignException.NotFound exception) {

            log.warn("OTP_LOGIN | DRIVER_NOT_FOUND | mobile={}", mobileNumber);

            throw new UserNotFoundException("Driver not found with this mobile number");
        }
    }

}