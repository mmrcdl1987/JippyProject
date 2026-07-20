
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
import com.jippy.foodandmart.exception.MerchantAlreadyExistsException;
import com.jippy.foodandmart.exception.OtpExpiredException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.repository.FmEmailOtpVerificationRepository;
import com.jippy.foodandmart.repository.FmMerchantRepository;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.repository.FmUserRepository;
import com.jippy.foodandmart.security.JwtUtils;
import com.jippy.foodandmart.service.EmailService;
import com.jippy.foodandmart.service.IFmOtpService;
import com.jippy.foodandmart.util.OtpGenerator;
import com.jippy.foodandmart.exception.EmailSendingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class FmOtpServiceImpl implements IFmOtpService {


    private final FmMerchantRepository merchantRepository;

    private final FmEmailOtpVerificationRepository otpRepository;

    private final RedisOtpService redisOtpService;

    private final OtpGenerator otpGenerator;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    public static final Integer EMAIL_OTP_EXPIRY_MINUTES = 10;

    public static final Integer MAX_OTP_ATTEMPTS = 5;

    public static final Integer MAX_RESEND_COUNT = 10;

    @Override
    @Transactional
    public void sendSignupOtp(FmSendOtpRequestDto request) {

        log.info("[OTP-SIGNUP] Send OTP request initiated | email={} | mobile={}",
                request.getEmail(),
                request.getMobile());

        if (merchantRepository.existsByMerchantEmail(request.getEmail())) {

            log.warn("[OTP-SIGNUP] Signup failed | Merchant already exists | email={}",
                    request.getEmail());

            throw new MerchantAlreadyExistsException(
                    "Merchant already exists with email : " + request.getEmail());
        }

        if (merchantRepository.existsByMerchantPhone(request.getMobile())) {

            log.warn("[OTP-SIGNUP] Signup failed | Merchant already exists | mobile={}",
                    request.getMobile());

            throw new MerchantAlreadyExistsException(
                    "Merchant already exists with mobile : " + request.getMobile());
        }

        String otp = otpGenerator.generateOtp();

        String otpHash = passwordEncoder.encode(otp);

        redisOtpService.saveOtpHash(
                "SIGNUP:" + request.getEmail(),
                otpHash
        );

        FmEmailOtpVerification otpVerification =
                otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(
                                request.getEmail(),
                                FmOtpPurpose.SIGNUP
                        ).filter(verification -> verification.getStatus() == FmOtpStatus.PENDING)
                        .orElse(new FmEmailOtpVerification());

        if (otpVerification.getEmailOtpVerificationId() == null) {

            otpVerification.setEntityType(FmUserType.MERCHANT);
            otpVerification.setEmail(request.getEmail());
            otpVerification.setPurpose(FmOtpPurpose.SIGNUP);
            otpVerification.setNoOfAttempts(0);
            otpVerification.setResendCount(0);

            log.info("[OTP-SIGNUP] Creating new OTP verification record | email={}",
                    request.getEmail());

        } else {

            log.info("[OTP-SIGNUP] Existing pending OTP found. Refreshing OTP | email={}",
                    request.getEmail());

            otpVerification.setNoOfAttempts(0);
        }

        otpVerification.setOtpHash(otpHash);
        otpVerification.setStatus(FmOtpStatus.PENDING);
        otpVerification.setIsVerified(false);
        otpVerification.setVerifiedAt(null);
        otpVerification.setExpiresAt(
                LocalDateTime.now().plusMinutes(EMAIL_OTP_EXPIRY_MINUTES)
        );

        otpRepository.save(otpVerification);

        try {

            emailService.sendOtpEmail(request.getEmail(), otp);

            log.info("[OTP-SIGNUP] OTP sent successfully | email={}",
                    request.getEmail());

        } catch (EmailSendingException ex) {

            redisOtpService.deleteOtp("SIGNUP:" + request.getEmail());

            otpVerification.setStatus(FmOtpStatus.FAILED);

            otpRepository.save(otpVerification);

            log.error("[OTP-SIGNUP] Failed to send OTP | email={} | reason={}",
                    request.getEmail(),
                    ex.getMessage(),
                    ex);

            throw ex;
        }

        log.info("[OTP-SIGNUP] Send OTP request completed successfully | email={}",
                request.getEmail());
    }

    @Override
    @Transactional
    public FmResponseDto verifySignupOtp(FmVerifyOtpRequestDto request) {

        log.info("[OTP-SIGNUP] OTP verification initiated | email={}",
                request.getEmail());

        FmEmailOtpVerification otpVerification =
                otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(
                        request.getEmail(),
                        FmOtpPurpose.SIGNUP
                ).orElseThrow(() -> {

                    log.warn("[OTP-SIGNUP] OTP verification failed | No OTP found | email={}",
                            request.getEmail());

                    return new InvalidOtpException("OTP not found. Please request a new OTP.");
                });

        if (Boolean.TRUE.equals(otpVerification.getIsVerified())) {

            log.warn("[OTP-SIGNUP] OTP already verified | email={}",
                    request.getEmail());

            throw new InvalidOtpException("OTP has already been verified.");
        }

        String redisOtpHash =
                redisOtpService.getOtpHash("SIGNUP:" + request.getEmail());

        if (redisOtpHash == null) {

            otpVerification.setStatus(FmOtpStatus.EXPIRED);
            otpRepository.save(otpVerification);

            redisOtpService.deleteOtp("SIGNUP:" + request.getEmail());

            log.warn("[OTP-SIGNUP] OTP expired | email={}",
                    request.getEmail());

            throw new OtpExpiredException(
                    "OTP has expired. Please request a new OTP."
            );
        }

        boolean validOtp =
                passwordEncoder.matches(request.getOtp(), redisOtpHash);

        if (!validOtp) {

            int attempts = otpVerification.getNoOfAttempts() + 1;

            otpVerification.setNoOfAttempts(attempts);

            if (attempts >=MAX_OTP_ATTEMPTS) {

                otpVerification.setStatus(FmOtpStatus.FAILED);

                otpRepository.save(otpVerification);

                redisOtpService.deleteOtp("SIGNUP:" + request.getEmail());

                log.warn("[OTP-SIGNUP] Maximum OTP verification attempts exceeded | email={} | attempts={}",
                        request.getEmail(),
                        attempts);

                throw new InvalidOtpException(
                        "Maximum OTP verification attempts exceeded. Please request a new OTP."
                );
            }

            otpRepository.save(otpVerification);

            log.warn("[OTP-SIGNUP] Invalid OTP entered | email={} | attempt={}",
                    request.getEmail(),
                    attempts);

            throw new InvalidOtpException("Invalid OTP.");
        }

        otpVerification.setIsVerified(true);
        otpVerification.setVerifiedAt(LocalDateTime.now());
        otpVerification.setStatus(FmOtpStatus.VERIFIED);

        otpRepository.save(otpVerification);

        redisOtpService.deleteOtp("SIGNUP:" + request.getEmail());

        log.info("[OTP-SIGNUP] OTP verified successfully | email={}",
                request.getEmail());

        return new FmResponseDto(
                "SUCCESS",
                "OTP verified successfully."
        );
    }
    @Override
    @Transactional
    public void sendCreateOutletOtp(FmCreateOutletOtpRequestDto request) {

        log.info("[OTP-OUTLET] Send OTP request initiated | merchantId={}",
                request.getMerchantId());

        FmMerchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> {

                    log.warn("[OTP-OUTLET] Merchant not found | merchantId={}",
                            request.getMerchantId());

                    return new ResourceNotFoundException(
                            "Merchant not found with id : " + request.getMerchantId());
                });

        String email = merchant.getMerchantEmail();

        String otp = otpGenerator.generateOtp();

        String otpHash = passwordEncoder.encode(otp);

        redisOtpService.saveOtpHash("CREATE_OUTLET:" + email, otpHash);

        Optional<FmEmailOtpVerification> optionalOtp =
                otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(
                        email,
                        FmOtpPurpose.CREATE_OUTLET);

        FmEmailOtpVerification otpVerification;

        if (optionalOtp.isPresent()
                && optionalOtp.get().getStatus() == FmOtpStatus.PENDING) {

            otpVerification = optionalOtp.get();

            log.info("[OTP-OUTLET] Existing pending OTP found. Refreshing OTP | merchantId={} | email={}",
                    merchant.getMerchantId(),
                    email);

            otpVerification.setOtpHash(otpHash);
            otpVerification.setExpiresAt(
                    LocalDateTime.now()
                            .plusMinutes(EMAIL_OTP_EXPIRY_MINUTES));
            otpVerification.setNoOfAttempts(0);
            otpVerification.setIsVerified(false);
            otpVerification.setVerifiedAt(null);
            otpVerification.setStatus(FmOtpStatus.PENDING);

        } else {

            log.info("[OTP-OUTLET] Creating new OTP verification record | merchantId={} | email={}",
                    merchant.getMerchantId(),
                    email);

            otpVerification = new FmEmailOtpVerification();

            otpVerification.setEntityId(merchant.getMerchantId());
            otpVerification.setEntityType(FmUserType.MERCHANT);
            otpVerification.setEmail(email);
            otpVerification.setOtpHash(otpHash);
            otpVerification.setPurpose(FmOtpPurpose.CREATE_OUTLET);
            otpVerification.setStatus(FmOtpStatus.PENDING);
            otpVerification.setExpiresAt(
                    LocalDateTime.now()
                            .plusMinutes(EMAIL_OTP_EXPIRY_MINUTES));
            otpVerification.setIsVerified(false);
            otpVerification.setNoOfAttempts(0);
            otpVerification.setResendCount(0);
        }

        otpRepository.save(otpVerification);

        try {

            emailService.sendOtpEmail(email, otp);

            log.info("[OTP-OUTLET] OTP sent successfully | merchantId={} | email={}",
                    merchant.getMerchantId(),
                    email);

        } catch (EmailSendingException ex) {

            redisOtpService.deleteOtp("CREATE_OUTLET:" + email);

            otpVerification.setStatus(FmOtpStatus.FAILED);

            otpRepository.save(otpVerification);

            log.error("[OTP-OUTLET] Failed to send OTP | merchantId={} | email={} | reason={}",
                    merchant.getMerchantId(),
                    email,
                    ex.getMessage(),
                    ex);

            throw ex;
        }

        log.info("[OTP-OUTLET] Send OTP request completed successfully | merchantId={} | email={}",
                merchant.getMerchantId(),
                email);
    }


    @Override
    @Transactional
    public FmResponseDto verifyCreateOutletOtp(FmVerifyOtpRequestDto request) {

        log.info("[OTP-OUTLET] OTP verification initiated | email={}",
                request.getEmail());

        merchantRepository.findByMerchantEmail(request.getEmail())
                .orElseThrow(() -> {

                    log.warn("[OTP-OUTLET] Merchant not found | email={}",
                            request.getEmail());

                    return new ResourceNotFoundException(
                            "Merchant not found with email : " + request.getEmail());
                });

        FmEmailOtpVerification otpVerification =
                otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(
                                request.getEmail(),
                                FmOtpPurpose.CREATE_OUTLET)
                        .orElseThrow(() -> {

                            log.warn("[OTP-OUTLET] OTP verification failed | No OTP found | email={}",
                                    request.getEmail());

                            return new InvalidOtpException(
                                    "OTP not found. Please request a new OTP.");
                        });

        if (otpVerification.getStatus() == FmOtpStatus.CONSUMED) {

            log.warn("[OTP-OUTLET] OTP already consumed | email={}",
                    request.getEmail());

            throw new InvalidOtpException(
                    "OTP has already been consumed.");
        }

        if (otpVerification.getStatus() == FmOtpStatus.VERIFIED) {

            log.warn("[OTP-OUTLET] OTP already verified | email={}",
                    request.getEmail());

            throw new InvalidOtpException(
                    "OTP has already been verified.");
        }

        String redisOtpHash =
                redisOtpService.getOtpHash("CREATE_OUTLET:" + request.getEmail());

        if (redisOtpHash == null) {

            otpVerification.setStatus(FmOtpStatus.EXPIRED);

            otpRepository.save(otpVerification);

            redisOtpService.deleteOtp("CREATE_OUTLET:" + request.getEmail());

            log.warn("[OTP-OUTLET] OTP expired | email={}",
                    request.getEmail());

            throw new OtpExpiredException(
                    "OTP has expired. Please request a new OTP.");
        }

        boolean validOtp =
                passwordEncoder.matches(request.getOtp(), redisOtpHash);

        if (!validOtp) {

            int attempts = otpVerification.getNoOfAttempts() + 1;

            otpVerification.setNoOfAttempts(attempts);

            if (attempts >= MAX_OTP_ATTEMPTS) {

                otpVerification.setStatus(FmOtpStatus.FAILED);

                otpRepository.save(otpVerification);

                redisOtpService.deleteOtp("CREATE_OUTLET:" + request.getEmail());

                log.warn("[OTP-OUTLET] Maximum OTP verification attempts exceeded | email={} | attempts={}",
                        request.getEmail(),
                        attempts);

                throw new InvalidOtpException(
                        "Maximum OTP verification attempts exceeded. Please request a new OTP.");
            }

            otpRepository.save(otpVerification);

            log.warn("[OTP-OUTLET] Invalid OTP entered | email={} | attempt={}",
                    request.getEmail(),
                    attempts);

            throw new InvalidOtpException("Invalid OTP.");
        }

        otpVerification.setIsVerified(true);
        otpVerification.setVerifiedAt(LocalDateTime.now());
        otpVerification.setStatus(FmOtpStatus.VERIFIED);

        otpRepository.save(otpVerification);

        redisOtpService.deleteOtp("CREATE_OUTLET:" + request.getEmail());

        log.info("[OTP-OUTLET] OTP verified successfully | email={}",
                request.getEmail());

        return new FmResponseDto(
                "SUCCESS",
                "OTP verified successfully."
        );
    }
    @Override
    @Transactional
    public void resendSignupOtp(FmSendOtpRequestDto request) {

        log.info("[OTP-SIGNUP] Resend OTP request initiated | email={}",
                request.getEmail());

        FmEmailOtpVerification otpVerification =
                otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(
                        request.getEmail(),
                        FmOtpPurpose.SIGNUP
                ).orElseThrow(() -> {

                    log.warn("[OTP-SIGNUP] Resend OTP failed | No OTP found | email={}",
                            request.getEmail());

                    return new InvalidOtpException(
                            "No OTP found. Please request a new OTP.");
                });

        if (otpVerification.getResendCount() >= MAX_RESEND_COUNT) {

            log.warn("[OTP-SIGNUP] Maximum resend limit exceeded | email={} | resendCount={}",
                    request.getEmail(),
                    otpVerification.getResendCount());

            throw new InvalidOtpException(
                    "Maximum resend limit reached. Please try again later.");
        }

        otpVerification.setResendCount(
                otpVerification.getResendCount() + 1);

        otpRepository.save(otpVerification);

        generateAndSendSignupOtp(request, otpVerification);

        log.info("[OTP-SIGNUP] OTP resent successfully | email={} | resendCount={}",
                request.getEmail(),
                otpVerification.getResendCount());
    }

    @Override
    @Transactional
    public void resendCreateOutletOtp(FmCreateOutletOtpRequestDto request) {

        log.info("[OTP-OUTLET] Resend OTP request initiated | merchantId={}",
                request.getMerchantId());

        FmMerchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> {

                    log.warn("[OTP-OUTLET] Merchant not found | merchantId={}",
                            request.getMerchantId());

                    return new ResourceNotFoundException("Merchant not found.");
                });

        FmEmailOtpVerification otpVerification =
                otpRepository.findTopByEntityTypeAndEntityIdAndPurposeOrderByCreatedAtDesc(
                        FmUserType.MERCHANT,
                        merchant.getMerchantId(),
                        FmOtpPurpose.CREATE_OUTLET
                ).orElseThrow(() -> {

                    log.warn("[OTP-OUTLET] Resend OTP failed | No OTP found | merchantId={}",
                            merchant.getMerchantId());

                    return new InvalidOtpException(
                            "No OTP found. Please request a new OTP.");
                });

        if (otpVerification.getResendCount() >= MAX_RESEND_COUNT) {

            log.warn("[OTP-OUTLET] Maximum resend limit exceeded | merchantId={} | resendCount={}",
                    merchant.getMerchantId(),
                    otpVerification.getResendCount());

            throw new InvalidOtpException(
                    "Maximum resend limit reached. Please try again later.");
        }

        otpVerification.setResendCount(
                otpVerification.getResendCount() + 1);

        otpRepository.save(otpVerification);

        generateAndSendCreateOutletOtp(
                merchant,
                otpVerification);

        log.info("[OTP-OUTLET] OTP resent successfully | merchantId={} | resendCount={}",
                merchant.getMerchantId(),
                otpVerification.getResendCount());
    }

    private void generateAndSendSignupOtp(
            FmSendOtpRequestDto request,
            FmEmailOtpVerification otpVerification) {

        String otp = otpGenerator.generateOtp();

        String otpHash = passwordEncoder.encode(otp);

        redisOtpService.saveOtpHash(
                "SIGNUP:" + request.getEmail(),
                otpHash);

        otpVerification.setOtpHash(otpHash);
        otpVerification.setStatus(FmOtpStatus.PENDING);
        otpVerification.setIsVerified(false);
        otpVerification.setVerifiedAt(null);
        otpVerification.setExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(EMAIL_OTP_EXPIRY_MINUTES));

        otpRepository.save(otpVerification);

        try {

            emailService.sendOtpEmail(request.getEmail(), otp);

            log.info("[OTP-SIGNUP] OTP email sent successfully | email={}",
                    request.getEmail());

        } catch (EmailSendingException ex) {

            redisOtpService.deleteOtp("SIGNUP:" + request.getEmail());

            otpVerification.setStatus(FmOtpStatus.FAILED);
            otpRepository.save(otpVerification);

            log.error("[OTP-SIGNUP] Failed to send OTP | email={} | reason={}",
                    request.getEmail(),
                    ex.getMessage(),
                    ex);

            throw ex;
        }
    }
    private void generateAndSendCreateOutletOtp(
            FmMerchant merchant,
            FmEmailOtpVerification otpVerification) {

        String otp = otpGenerator.generateOtp();

        String otpHash = passwordEncoder.encode(otp);

        redisOtpService.saveOtpHash(
                "CREATE_OUTLET:" + merchant.getMerchantEmail(),
                otpHash);

        otpVerification.setOtpHash(otpHash);
        otpVerification.setStatus(FmOtpStatus.PENDING);
        otpVerification.setIsVerified(false);
        otpVerification.setVerifiedAt(null);
        otpVerification.setExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(EMAIL_OTP_EXPIRY_MINUTES));

        otpRepository.save(otpVerification);

        try {

            emailService.sendOtpEmail(
                    merchant.getMerchantEmail(),
                    otp);

            log.info("[OTP-OUTLET] OTP email sent successfully | merchantId={} | email={}",
                    merchant.getMerchantId(),
                    merchant.getMerchantEmail());

        } catch (EmailSendingException ex) {

            redisOtpService.deleteOtp(
                    "CREATE_OUTLET:" + merchant.getMerchantEmail());

            otpVerification.setStatus(FmOtpStatus.FAILED);

            otpRepository.save(otpVerification);

            log.error("[OTP-OUTLET] Failed to send OTP | merchantId={} | email={} | reason={}",
                    merchant.getMerchantId(),
                    merchant.getMerchantEmail(),
                    ex.getMessage(),
                    ex);

            throw ex;
        }
    }

//    @Transactional
//    public void sendForgotPasswordOtp(FmForgotPasswordRequestDto request) {
//
//        log.info("Sending forgot password OTP for username: {}", request.getUsername());
//
//        // Find user
//        FmUser user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User not found with username : " + request.getUsername()));
//
//        String email;
//
//        if (FmUserType.MERCHANT.name().equals(user.getUserType())) {
//
//            FmMerchant merchant = merchantRepository.findById(user.getUserId()).orElseThrow(() -> new ResourceNotFoundException("Merchant not found."));
//
//            email = merchant.getMerchantEmail();
//
//        } else if (FmUserType.OUTLET.name().equals(user.getUserType())) {
//
//            FmOutlet outlet = outletRepository.findById(user.getUserId()).orElseThrow(() -> new ResourceNotFoundException("Outlet not found."));
//
//            FmMerchant merchant = merchantRepository.findById(outlet.getMerchantId()).orElseThrow(() -> new ResourceNotFoundException("Merchant not found."));
//
//            email = merchant.getMerchantEmail();
//
//        } else {
//
//            throw new ResourceNotFoundException("Forgot password is not supported for user type : " + user.getUserType());
//        }
//
//        // Generate OTP
//        String otp = otpGenerator.generateOtp();
//
//        // BCrypt hash
//        String otpHash = passwordEncoder.encode(otp);
//
//        // Save in Redis
//        redisOtpService.saveOtpHash("FORGOT_PASSWORD:" + email, otpHash);
//
//        Optional<FmEmailOtpVerification> optionalOtp = otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, FmOtpPurpose.FORGOT_PASSWORD);
//
//        FmEmailOtpVerification otpVerification;
//
//        if (optionalOtp.isPresent() && optionalOtp.get().getStatus() == FmOtpStatus.PENDING) {
//
//            otpVerification = optionalOtp.get();
//
//            otpVerification.setOtpHash(otpHash);
//            otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
//            otpVerification.setNoOfAttempts(0);
//            otpVerification.setIsVerified(false);
//            otpVerification.setVerifiedAt(null);
//            otpVerification.setStatus(FmOtpStatus.PENDING);
//
//        } else {
//
//            otpVerification = new FmEmailOtpVerification();
//
//            otpVerification.setEntityId(user.getUserId());
//            otpVerification.setEntityType(FmUserType.valueOf(user.getUserType()));
//            otpVerification.setEmail(email);
//            otpVerification.setOtpHash(otpHash);
//            otpVerification.setPurpose(FmOtpPurpose.FORGOT_PASSWORD);
//            otpVerification.setStatus(FmOtpStatus.PENDING);
//            otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
//            otpVerification.setIsVerified(false);
//            otpVerification.setNoOfAttempts(0);
//        }
//
//        otpRepository.save(otpVerification);
//
//        try {
//
//            emailService.sendForgotPasswordOtp(email, otp);
//
//            log.info("Forgot password OTP sent successfully to {}", email);
//
//        } catch (EmailSendingException ex) {
//
//            log.error("Failed to send forgot password OTP to {}", email, ex);
//
//            redisOtpService.deleteOtp("FORGOT_PASSWORD:" + email);
//
//            otpVerification.setStatus(FmOtpStatus.FAILED);
//
//            otpRepository.save(otpVerification);
//
//            throw ex;
//        }
//    }

//
//    @Transactional
//    public FmResponseDto verifyForgotPasswordOtp(FmVerifyOtpRequestDto request) {
//
//        log.info("Verifying forgot password OTP for email: {}", request.getEmail());
//
//        FmEmailOtpVerification otpVerification = otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(request.getEmail(), FmOtpPurpose.FORGOT_PASSWORD).orElseThrow(() -> new InvalidOtpException("OTP not found."));
//
//        if (otpVerification.getStatus() == FmOtpStatus.CONSUMED) {
//            throw new InvalidOtpException("OTP already consumed.");
//        }
//
//        if (otpVerification.getStatus() == FmOtpStatus.VERIFIED) {
//            throw new InvalidOtpException("OTP already verified.");
//        }
//
//        String redisOtpHash = redisOtpService.getOtpHash("FORGOT_PASSWORD:" + request.getEmail());
//
//        if (redisOtpHash == null) {
//
//            otpVerification.setStatus(FmOtpStatus.EXPIRED);
//            otpRepository.save(otpVerification);
//
//            redisOtpService.deleteOtp("FORGOT_PASSWORD:" + request.getEmail());
//
//            throw new OtpExpiredException("OTP has expired.");
//        }
//
//        if (!passwordEncoder.matches(request.getOtp(), redisOtpHash)) {
//
//            int attempts = otpVerification.getNoOfAttempts() + 1;
//            otpVerification.setNoOfAttempts(attempts);
//
//            if (attempts >= 5) {
//
//                otpVerification.setStatus(FmOtpStatus.FAILED);
//                otpRepository.save(otpVerification);
//
//                redisOtpService.deleteOtp("FORGOT_PASSWORD:" + request.getEmail());
//
//                throw new InvalidOtpException("Maximum OTP attempts exceeded.");
//            }
//
//            otpRepository.save(otpVerification);
//
//            throw new InvalidOtpException("Invalid OTP.");
//        }
//
//        otpVerification.setIsVerified(true);
//        otpVerification.setVerifiedAt(LocalDateTime.now());
//        otpVerification.setStatus(FmOtpStatus.VERIFIED);
//
//        otpRepository.save(otpVerification);
//
//        redisOtpService.deleteOtp("FORGOT_PASSWORD:" + request.getEmail());
//
//        log.info("Forgot password OTP verified successfully for email: {}", request.getEmail());
//
//        return new FmResponseDto("SUCCESS", "OTP verified successfully.");
//    }

}
