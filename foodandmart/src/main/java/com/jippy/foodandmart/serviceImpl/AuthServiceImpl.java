
package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.Enum.FmOtpPurpose;
import com.jippy.foodandmart.Enum.FmOtpStatus;
import com.jippy.foodandmart.Enum.FmUserType;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmEmailOtpVerification;
import com.jippy.foodandmart.entity.FmMerchant;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.entity.FmUser;
import com.jippy.foodandmart.exception.InvalidOtpException;
import com.jippy.foodandmart.exception.InvalidTokenException;
import com.jippy.foodandmart.exception.MerchantAlreadyExistsException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.repository.FmEmailOtpVerificationRepository;
import com.jippy.foodandmart.repository.FmMerchantRepository;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.repository.FmUserRepository;
import com.jippy.foodandmart.security.JwtUtils;
import com.jippy.foodandmart.service.EmailService;
import com.jippy.foodandmart.service.IFmAuthService;
import com.jippy.foodandmart.service.IFmMerchantService;
import com.jippy.foodandmart.service.IFmOtpService;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IFmAuthService {

    private final JwtUtils jwtUtils;
    private final IFmMerchantService merchantService;
    private final FmMerchantRepository merchantRepository;
    private final FmEmailOtpVerificationRepository otpRepository;
    private final EmailService emailService;
    private final IFmOtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final FmUserRepository userRepository;
    private final FmOutletRepository outletRepository;

    @Override
    @Transactional
    public FmResponseDto signupMerchant(String signupToken, FmMerchantRequestDTO request) {

        log.info("Merchant signup started.");

        // Validate Signup Token

        if (!jwtUtils.isTokenValid(signupToken)) {
            throw new InvalidTokenException("Signup token expired.");
        }

        Claims claims = jwtUtils.getClaims(signupToken);

        String verifiedEmail = claims.getSubject();

        FmOtpPurpose purpose = FmOtpPurpose.valueOf(claims.get("purpose", String.class));

        Boolean verified = claims.get("verified", Boolean.class);

        if (purpose != FmOtpPurpose.SIGNUP) {
            throw new InvalidTokenException("Invalid signup token.");
        }

        if (!Boolean.TRUE.equals(verified)) {
            throw new InvalidTokenException("OTP not verified.");
        }

        // Always trust verified email from JWT

        request.setEmail(verifiedEmail);

        // Double-check merchant email

        if (merchantRepository.existsByMerchantEmail(verifiedEmail)) {
            throw new MerchantAlreadyExistsException("Merchant already exists with email : " + verifiedEmail);
        }

        // Create Merchant
        // Existing service creates:
        // Merchant
        // Merchant KYC
        // Merchant Bank
        // Merchant User
        // User Role Mapping

        FmMerchant merchant = merchantService.createMerchant(request);

        log.info("Merchant created successfully. MerchantId={}", merchant.getMerchantId());

        // Update OTP Audit

        otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(verifiedEmail, FmOtpPurpose.SIGNUP).ifPresent(otp -> {
            otp.setEntityId(merchant.getMerchantId());
            otpRepository.save(otp);
        });

        // Welcome Email

        try {

            emailService.sendWelcomeEmail(merchant.getMerchantEmail(), merchant.getMerchantName());

        } catch (Exception ex) {

            log.error("Failed to send welcome email to {}", merchant.getMerchantEmail(), ex);
        }

        log.info("Merchant signup completed successfully. MerchantId={}", merchant.getMerchantId());

        return new FmResponseDto("SUCCESS", "Merchant registered successfully.");
    }

    @Override
    @Transactional
    public FmJwtTokenResponseDto verifySignupOtp(FmVerifyOtpRequestDto request) {

        log.info("Verify signup OTP requested for email: {}", request.getEmail());

        return otpService.verifySignupOtp(request);
    }

    @Override
    @Transactional
    public FmResponseDto resetPassword(FmResetPasswordRequestDto request) {

        log.info("Reset password requested for username: {}", request.getUsername());

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match.");
        }

        // Find User
        FmUser user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User not found with username : " + request.getUsername()));

        String email;

        if (FmUserType.MERCHANT.name().equals(user.getUserType())) {

            FmMerchant merchant = merchantRepository.findById(user.getUserId()).orElseThrow(() -> new ResourceNotFoundException("Merchant not found."));

            email = merchant.getMerchantEmail();

        } else if (FmUserType.OUTLET.name().equals(user.getUserType())) {

            FmOutlet outlet = outletRepository.findById(user.getUserId()).orElseThrow(() -> new ResourceNotFoundException("Outlet not found."));

            FmMerchant merchant = merchantRepository.findById(outlet.getMerchantId()).orElseThrow(() -> new ResourceNotFoundException("Merchant not found."));

            email = merchant.getMerchantEmail();

        } else {

            throw new IllegalArgumentException("Forgot password is not supported for user type : " + user.getUserType());
        }

        // Verify latest Forgot Password OTP
        FmEmailOtpVerification otpVerification = otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, FmOtpPurpose.FORGOT_PASSWORD).orElseThrow(() -> new InvalidOtpException("Please verify OTP before resetting password."));

        if (otpVerification.getStatus() != FmOtpStatus.VERIFIED) {

            throw new InvalidOtpException("Please verify OTP before resetting password.");
        }

        // Update Password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        // Consume OTP
        otpVerification.setStatus(FmOtpStatus.CONSUMED);
        otpRepository.save(otpVerification);

        log.info("Password reset successfully for username: {}", request.getUsername());

        return new FmResponseDto("SUCCESS", "Password reset successfully.");
    }
}
