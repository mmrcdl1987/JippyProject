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

    private final FmOutletRepository outletRepository;

    private final FmUserRepository userRepository;

    private final FmEmailOtpVerificationRepository otpRepository;

    private final RedisOtpService redisOtpService;

    private final OtpGenerator otpGenerator;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public void sendSignupOtp(FmSendOtpRequestDto request) {

        log.info("Sending signup OTP to email: {}", request.getEmail());

        // Validate merchant email
        if (merchantRepository.existsByMerchantEmail(request.getEmail())) {
            throw new MerchantAlreadyExistsException("Merchant already exists with email : " + request.getEmail());
        }

        // Validate merchant mobile
        if (merchantRepository.existsByMerchantPhone(request.getMobile())) {
            throw new MerchantAlreadyExistsException("Merchant already exists with mobile : " + request.getMobile());
        }

        // Generate OTP
        String otp = otpGenerator.generateOtp();

        // BCrypt hash
        String otpHash = passwordEncoder.encode(otp);

        // Save BCrypt hash in Redis
        redisOtpService.saveOtpHash("SIGNUP:" + request.getEmail(), otpHash);

        // Check existing pending OTP
        Optional<FmEmailOtpVerification> optionalOtp = otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(request.getEmail(), FmOtpPurpose.SIGNUP);

        FmEmailOtpVerification otpVerification;

        if (optionalOtp.isPresent() && optionalOtp.get().getStatus() == FmOtpStatus.PENDING) {

            otpVerification = optionalOtp.get();

            otpVerification.setOtpHash(otpHash);
            otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            otpVerification.setNoOfAttempts(0);
            otpVerification.setIsVerified(false);
            otpVerification.setVerifiedAt(null);
            otpVerification.setStatus(FmOtpStatus.PENDING);

        } else {

            otpVerification = new FmEmailOtpVerification();

            otpVerification.setEntityType(FmUserType.MERCHANT);
            otpVerification.setEmail(request.getEmail());
            otpVerification.setOtpHash(otpHash);
            otpVerification.setPurpose(FmOtpPurpose.SIGNUP);
            otpVerification.setStatus(FmOtpStatus.PENDING);
            otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            otpVerification.setIsVerified(false);
            otpVerification.setNoOfAttempts(0);

        }

        otpRepository.save(otpVerification);

        try {

            emailService.sendOtpEmail(request.getEmail(), otp);

            log.info("Signup OTP sent successfully to {}", request.getEmail());

        } catch (EmailSendingException ex) {

            log.error("Failed to send signup OTP to {}", request.getEmail(), ex);

            redisOtpService.deleteOtp("SIGNUP:" + request.getEmail());

            otpVerification.setStatus(FmOtpStatus.FAILED);

            otpRepository.save(otpVerification);

            throw ex;
        }
    }

    @Override
    @Transactional
    public FmJwtTokenResponseDto verifySignupOtp(FmVerifyOtpRequestDto request) {

        log.info("Verifying signup OTP for email: {}", request.getEmail());

        // Fetch latest OTP audit record
        FmEmailOtpVerification otpVerification = otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(request.getEmail(), FmOtpPurpose.SIGNUP).orElseThrow(() -> new InvalidOtpException("OTP not found."));

        // Prevent reusing an already verified OTP
        if (Boolean.TRUE.equals(otpVerification.getIsVerified())) {
            throw new InvalidOtpException("OTP already verified.");
        }

        // Read OTP hash from Redis
        String redisOtpHash = redisOtpService.getOtpHash("SIGNUP:" + request.getEmail());

        if (redisOtpHash == null) {

            otpVerification.setStatus(FmOtpStatus.EXPIRED);
            otpRepository.save(otpVerification);

            redisOtpService.deleteOtp("SIGNUP:" + request.getEmail());

            log.warn("OTP expired for email: {}", request.getEmail());

            throw new OtpExpiredException("OTP has expired.");
        }

        // Verify OTP
        boolean validOtp = passwordEncoder.matches(request.getOtp(), redisOtpHash);

        if (!validOtp) {

            int attempts = otpVerification.getNoOfAttempts() + 1;
            otpVerification.setNoOfAttempts(attempts);

            if (attempts >= 5) {

                otpVerification.setStatus(FmOtpStatus.FAILED);

                otpRepository.save(otpVerification);

                redisOtpService.deleteOtp("SIGNUP:" + request.getEmail());

                log.warn("Maximum OTP attempts exceeded for email: {}", request.getEmail());

                throw new InvalidOtpException("Maximum OTP attempts exceeded.");
            }

            otpRepository.save(otpVerification);

            log.warn("Invalid OTP entered for email: {}. Attempt: {}", request.getEmail(), attempts);

            throw new InvalidOtpException("Invalid OTP.");
        }

        // Mark OTP as verified
        otpVerification.setIsVerified(true);
        otpVerification.setVerifiedAt(LocalDateTime.now());
        otpVerification.setStatus(FmOtpStatus.VERIFIED);

        otpRepository.save(otpVerification);

        // Remove OTP from Redis
        redisOtpService.deleteOtp("SIGNUP:" + request.getEmail());

        // Generate Signup JWT (15 minutes)
        String signupToken = jwtUtils.generateSignupToken(request.getEmail(), FmOtpPurpose.SIGNUP);

        log.info("Signup OTP verified successfully for email: {}", request.getEmail());

        return new FmJwtTokenResponseDto(signupToken, "OTP verified successfully.");
    }

    @Override
    @Transactional
    public void sendCreateOutletOtp(FmCreateOutletOtpRequestDto request) {

        log.info("Sending create outlet OTP for merchantId: {}", request.getMerchantId());

        // Fetch Merchant
        FmMerchant merchant = merchantRepository.findById(request.getMerchantId()).orElseThrow(() -> new ResourceNotFoundException("Merchant not found with id : " + request.getMerchantId()));

        String email = merchant.getMerchantEmail();

        // Generate OTP
        String otp = otpGenerator.generateOtp();

        // BCrypt Hash
        String otpHash = passwordEncoder.encode(otp);

        // Save OTP Hash in Redis
        redisOtpService.saveOtpHash("CREATE_OUTLET:" + email, otpHash);

        // Check existing pending OTP
        Optional<FmEmailOtpVerification> optionalOtp = otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, FmOtpPurpose.CREATE_OUTLET);

        FmEmailOtpVerification otpVerification;

        if (optionalOtp.isPresent() && optionalOtp.get().getStatus() == FmOtpStatus.PENDING) {

            otpVerification = optionalOtp.get();

            otpVerification.setOtpHash(otpHash);
            otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            otpVerification.setNoOfAttempts(0);
            otpVerification.setIsVerified(false);
            otpVerification.setVerifiedAt(null);
            otpVerification.setStatus(FmOtpStatus.PENDING);

        } else {

            otpVerification = new FmEmailOtpVerification();

            otpVerification.setEntityId(merchant.getMerchantId());
            otpVerification.setEntityType(FmUserType.MERCHANT);
            otpVerification.setEmail(email);
            otpVerification.setOtpHash(otpHash);
            otpVerification.setPurpose(FmOtpPurpose.CREATE_OUTLET);
            otpVerification.setStatus(FmOtpStatus.PENDING);
            otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            otpVerification.setIsVerified(false);
            otpVerification.setNoOfAttempts(0);

        }

        otpRepository.save(otpVerification);

        try {

            emailService.sendOtpEmail(email, otp);

            log.info("Create outlet OTP sent successfully to {}", email);

        } catch (EmailSendingException ex) {

            log.error("Failed to send create outlet OTP to {}", email, ex);

            redisOtpService.deleteOtp("CREATE_OUTLET:" + email);

            otpVerification.setStatus(FmOtpStatus.FAILED);

            otpRepository.save(otpVerification);

            throw ex;
        }
    }


    @Override
    @Transactional
    public FmResponseDto verifyCreateOutletOtp(FmVerifyOtpRequestDto request) {

        log.info("Verifying create outlet OTP for email: {}", request.getEmail());

        merchantRepository.findByMerchantEmail(request.getEmail()).orElseThrow(() -> new ResourceNotFoundException("Merchant not found with email : " + request.getEmail()));

        FmEmailOtpVerification otpVerification = otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(request.getEmail(), FmOtpPurpose.CREATE_OUTLET).orElseThrow(() -> new InvalidOtpException("OTP not found."));

        if (otpVerification.getStatus() == FmOtpStatus.CONSUMED) {
            throw new InvalidOtpException("OTP already consumed.");
        }

        if (otpVerification.getStatus() == FmOtpStatus.VERIFIED) {
            throw new InvalidOtpException("OTP already verified.");
        }

        String redisOtpHash = redisOtpService.getOtpHash("CREATE_OUTLET:" + request.getEmail());

        if (redisOtpHash == null) {

            otpVerification.setStatus(FmOtpStatus.EXPIRED);
            otpRepository.save(otpVerification);

            redisOtpService.deleteOtp("CREATE_OUTLET:" + request.getEmail());

            throw new OtpExpiredException("OTP has expired.");
        }

        if (!passwordEncoder.matches(request.getOtp(), redisOtpHash)) {

            int attempts = otpVerification.getNoOfAttempts() + 1;
            otpVerification.setNoOfAttempts(attempts);

            if (attempts >= 5) {

                otpVerification.setStatus(FmOtpStatus.FAILED);
                otpRepository.save(otpVerification);

                redisOtpService.deleteOtp("CREATE_OUTLET:" + request.getEmail());

                throw new InvalidOtpException("Maximum OTP attempts exceeded.");
            }

            otpRepository.save(otpVerification);

            throw new InvalidOtpException("Invalid OTP.");
        }

        otpVerification.setIsVerified(true);
        otpVerification.setVerifiedAt(LocalDateTime.now());
        otpVerification.setStatus(FmOtpStatus.VERIFIED);

        otpRepository.save(otpVerification);

        redisOtpService.deleteOtp("CREATE_OUTLET:" + request.getEmail());

        log.info("Create outlet OTP verified successfully for email: {}", request.getEmail());

        return new FmResponseDto("SUCCESS", "OTP verified successfully.");
    }

    @Override
    @Transactional
    public void sendForgotPasswordOtp(FmForgotPasswordRequestDto request) {

        log.info("Sending forgot password OTP for username: {}", request.getUsername());

        // Find user
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

            throw new ResourceNotFoundException("Forgot password is not supported for user type : " + user.getUserType());
        }

        // Generate OTP
        String otp = otpGenerator.generateOtp();

        // BCrypt hash
        String otpHash = passwordEncoder.encode(otp);

        // Save in Redis
        redisOtpService.saveOtpHash("FORGOT_PASSWORD:" + email, otpHash);

        Optional<FmEmailOtpVerification> optionalOtp = otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, FmOtpPurpose.FORGOT_PASSWORD);

        FmEmailOtpVerification otpVerification;

        if (optionalOtp.isPresent() && optionalOtp.get().getStatus() == FmOtpStatus.PENDING) {

            otpVerification = optionalOtp.get();

            otpVerification.setOtpHash(otpHash);
            otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            otpVerification.setNoOfAttempts(0);
            otpVerification.setIsVerified(false);
            otpVerification.setVerifiedAt(null);
            otpVerification.setStatus(FmOtpStatus.PENDING);

        } else {

            otpVerification = new FmEmailOtpVerification();

            otpVerification.setEntityId(user.getUserId());
            otpVerification.setEntityType(FmUserType.valueOf(user.getUserType()));
            otpVerification.setEmail(email);
            otpVerification.setOtpHash(otpHash);
            otpVerification.setPurpose(FmOtpPurpose.FORGOT_PASSWORD);
            otpVerification.setStatus(FmOtpStatus.PENDING);
            otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            otpVerification.setIsVerified(false);
            otpVerification.setNoOfAttempts(0);
        }

        otpRepository.save(otpVerification);

        try {

            emailService.sendForgotPasswordOtp(email, otp);

            log.info("Forgot password OTP sent successfully to {}", email);

        } catch (EmailSendingException ex) {

            log.error("Failed to send forgot password OTP to {}", email, ex);

            redisOtpService.deleteOtp("FORGOT_PASSWORD:" + email);

            otpVerification.setStatus(FmOtpStatus.FAILED);

            otpRepository.save(otpVerification);

            throw ex;
        }
    }

    @Override
    @Transactional
    public FmResponseDto verifyForgotPasswordOtp(FmVerifyOtpRequestDto request) {

        log.info("Verifying forgot password OTP for email: {}", request.getEmail());

        FmEmailOtpVerification otpVerification = otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(request.getEmail(), FmOtpPurpose.FORGOT_PASSWORD).orElseThrow(() -> new InvalidOtpException("OTP not found."));

        if (otpVerification.getStatus() == FmOtpStatus.CONSUMED) {
            throw new InvalidOtpException("OTP already consumed.");
        }

        if (otpVerification.getStatus() == FmOtpStatus.VERIFIED) {
            throw new InvalidOtpException("OTP already verified.");
        }

        String redisOtpHash = redisOtpService.getOtpHash("FORGOT_PASSWORD:" + request.getEmail());

        if (redisOtpHash == null) {

            otpVerification.setStatus(FmOtpStatus.EXPIRED);
            otpRepository.save(otpVerification);

            redisOtpService.deleteOtp("FORGOT_PASSWORD:" + request.getEmail());

            throw new OtpExpiredException("OTP has expired.");
        }

        if (!passwordEncoder.matches(request.getOtp(), redisOtpHash)) {

            int attempts = otpVerification.getNoOfAttempts() + 1;
            otpVerification.setNoOfAttempts(attempts);

            if (attempts >= 5) {

                otpVerification.setStatus(FmOtpStatus.FAILED);
                otpRepository.save(otpVerification);

                redisOtpService.deleteOtp("FORGOT_PASSWORD:" + request.getEmail());

                throw new InvalidOtpException("Maximum OTP attempts exceeded.");
            }

            otpRepository.save(otpVerification);

            throw new InvalidOtpException("Invalid OTP.");
        }

        otpVerification.setIsVerified(true);
        otpVerification.setVerifiedAt(LocalDateTime.now());
        otpVerification.setStatus(FmOtpStatus.VERIFIED);

        otpRepository.save(otpVerification);

        redisOtpService.deleteOtp("FORGOT_PASSWORD:" + request.getEmail());

        log.info("Forgot password OTP verified successfully for email: {}", request.getEmail());

        return new FmResponseDto("SUCCESS", "OTP verified successfully.");
    }

}