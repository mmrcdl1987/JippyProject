package com.jippy.customerandorder.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.entity.CustomerOtp;
import com.jippy.customerandorder.entity.CustomerStatus;
import com.jippy.customerandorder.exception.*;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.OtpService;
import com.jippy.customerandorder.iservice.SmsCountryService;
import com.jippy.customerandorder.repository.CoCustomerRepository;
import com.jippy.customerandorder.repository.CustomerOtpRepository;
import com.jippy.customerandorder.repository.CustomerStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private static final Integer OTP_EXPIRY_MINUTES = 10;
    private static final Integer MAX_RETRY_COUNT = 5;
    private static final Integer MAX_RESEND_COUNT = 3;
    private static final Integer DAILY_OTP_LIMIT = 20;

    private final CoCustomerRepository customerRepository;
    private final CustomerOtpRepository customerOtpRepository;
    private final SmsCountryService smsCountryService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CustomerStatusRepository customerStatusRepository;
    private final FMFeignClient fmFeignClient;

    @Override
    public ApiResponseDto sendOtp(SendOtpRequestDto request) {
        MDC.put("operation", "SEND_OTP");
        MDC.put("mobile", request.getMobileNumber());
        log.info("OTP_SERVICE | SEND_OTP | START");

        CoCustomer customer = null;
        String lockKey = null;
        try {
            String mobileNumber = normalizeMobile(request.getMobileNumber());

            customer = customerRepository.findByPhoneNumber(mobileNumber).orElseGet(() -> createCustomer(mobileNumber));

            MDC.put("customerId", customer.getCustomerId() == null ? "-" : String.valueOf(customer.getCustomerId()));
            log.info("OTP_SERVICE | SEND_OTP | resolvedCustomer");

            validateCustomerStatus(customer);
            validateDailyLimit(customer);
            validateOtpRateLimit(customer.getPhoneNumber());

            lockKey = getOtpLockKey(customer.getPhoneNumber());
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(5));

            if (!Boolean.TRUE.equals(locked)) {
                log.warn("OTP_SERVICE | SEND_OTP | LOCK_ACQUIRE_FAILED");
                throw new CoBadRequestException("OTP generation already in progress");
            }

            log.debug("OTP_SERVICE | SEND_OTP | invalidating old OTP if any");
            invalidateOldOtp(customer);

            String otp = generateOtp(); // DO NOT log OTP
            String otpHash = passwordEncoder.encode(otp);

            String referenceId = smsCountryService.sendOtp(customer.getPhoneNumber(), otp);
            log.info("SMS Reference Id = {}", referenceId);
            log.info("OTP_SERVICE | SEND_OTP | smsSent | referenceId={}", referenceId);

            saveOtpInRedis(customer, otpHash, 0);
            log.debug("OTP_SERVICE | SEND_OTP | cachedOtpInRedis");

            CustomerOtp customerOtp = saveOtpTransaction(customer, otpHash, 0);
            customerOtp.setOtpReferenceId(referenceId);
            customerOtpRepository.saveAndFlush(customerOtp);

            log.info("OTP_SERVICE | SEND_OTP | otpSaved | otpId={}", customerOtp.getCustomerOtpId());

            log.info("OTP_SERVICE | SEND_OTP | SUCCESS");
            return new ApiResponseDto(true, "OTP sent successfully");

        } catch (Exception ex) {
            log.error("OTP_SERVICE | SEND_OTP | ERROR | message={}", ex.getMessage(), ex);
            throw ex;
        } finally {
            if (lockKey != null) {
                redisTemplate.delete(lockKey);
                log.debug("OTP_SERVICE | SEND_OTP | lockReleased");
            }
            MDC.remove("operation");
            MDC.remove("mobile");
            MDC.remove("customerId");
        }
    }

    @Override
    public JwtResponseDto verifyOtp(VerifyOtpRequestDto request) {
        MDC.put("operation", "VERIFY_OTP");
        MDC.put("mobile", request.getMobileNumber());
        log.info("OTP_SERVICE | VERIFY_OTP | START");
        try {
            String mobileNumber = normalizeMobile(request.getMobileNumber());
            CoCustomer customer = customerRepository.findByPhoneNumber(mobileNumber)

                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

            MDC.put("customerId", customer.getCustomerId() == null ? "-" : String.valueOf(customer.getCustomerId()));
            log.debug("OTP_SERVICE | VERIFY_OTP | resolvedCustomer");

            validateCustomerStatus(customer);

            CustomerOtp customerOtp = customerOtpRepository.findTopByCustomerAndIsUsedFalseOrderByCustomerOtpIdDesc(customer).orElseThrow(() -> new OtpNotFoundException("OTP not found"));

            validateOtp(customerOtp);
            validateOtpFormat(request.getOtp());

            String redisKey = getRedisKey(customer.getCustomerId());
            OtpCacheDto cache = (OtpCacheDto) redisTemplate.opsForValue().get(redisKey);

            if (cache == null) {
                log.warn("OTP_SERVICE | VERIFY_OTP | cacheMissing | markingOtpUsed");
                customerOtp.setIsUsed(true);
                customerOtp.setUpdatedAt(LocalDateTime.now());
                customerOtpRepository.saveAndFlush(customerOtp);
                throw new OtpExpiredException("OTP expired");
            }
            validateOtpCache(cache);

            if (customer.getCustomerId().longValue() != cache.getCustomerId()) {

                log.error("OTP_SERVICE | VERIFY_OTP | customerIdMismatch | dbCustomerId={} | cacheCustomerId={}", customer.getCustomerId(), cache.getCustomerId());

                throw new OtpExpiredException("Invalid OTP session");
            }

            if (!passwordEncoder.matches(request.getOtp(), cache.getOtpHash())) {

                log.warn("OTP_SERVICE | VERIFY_OTP | invalidOtpAttempt | retryCount={}", cache.getRetryCount());

                handleInvalidOtp(customerOtp, cache, redisKey);

                throw new InvalidOtpException("Invalid OTP");
            }

            customerOtp.setIsVerified(true);
            customerOtp.setIsUsed(true);
            customerOtp.setUpdatedAt(LocalDateTime.now());
            customerOtpRepository.saveAndFlush(customerOtp);

            redisTemplate.delete(redisKey);

            ResponseEntity<CoUserDto> existingUser = fmFeignClient.
                    findByUserIdAndUserType(customer.getCustomerId(),COConstants.CUSTOMER);
            CoUserDto existingUserDto = existingUser.getBody();
            if (existingUserDto.getUserId() == null) {
                CoUserDto userDto = new CoUserDto();

                userDto.setUsername(mobileNumber);
                userDto.setUserId(customer.getCustomerId());
                userDto.setUserType(COConstants.CUSTOMER);
                userDto.setPassword(customer.getCustomerId()+mobileNumber);
                try{
                    ResponseEntity<CoUserDto> userDtoResponseEntity = fmFeignClient.createUser(userDto);
                    existingUserDto = userDtoResponseEntity.getBody();
                } catch (Exception e) {
                    log.error("User creation failed in FM", e);
                    throw new RuntimeException(e);
                }

                log.info("User created in FM for CustomerId: {}", customer.getCustomerId());
            }

            CoLoginDto loginDto = new CoLoginDto();

            loginDto.setUsername(mobileNumber);
            loginDto.setPassword(customer.getCustomerId()+mobileNumber);
            ResponseEntity<?> userDtoResponseEntity =  fmFeignClient.login(loginDto);
            Object body = userDtoResponseEntity.getBody();

            ObjectMapper mapper = new ObjectMapper();
            // Safely convert the LinkedHashMap to your desired DTO
            AuthResponseDto responseDto = mapper.convertValue(body, AuthResponseDto.class);

            JwtResponseDto response = new JwtResponseDto();
            response.setCustomerId(customer.getCustomerId().longValue());
            response.setMobileNumber(customer.getPhoneNumber());
            response.setFirstName(customer.getFirstName());
            response.setLastName(customer.getLastName());
            response.setAccessToken(responseDto.getJwt());
            response.setExpiresIn(24 * 60 * 60L);

            log.info("OTP_SERVICE | VERIFY_OTP | SUCCESS");
            return response;
        } catch (Exception ex) {
            log.error("OTP_SERVICE | VERIFY_OTP | ERROR | message={}", ex.getMessage(), ex);
            throw ex;
        } finally {
            MDC.remove("operation");
            MDC.remove("mobile");
            MDC.remove("customerId");
        }
    }

    @Override
    public ApiResponseDto resendOtp(SendOtpRequestDto request) {
        MDC.put("operation", "RESEND_OTP");
        MDC.put("mobile", request.getMobileNumber());
        log.info("OTP_SERVICE | RESEND_OTP | START");
        String lockKey = null;
        try {
            String mobileNumber = normalizeMobile(request.getMobileNumber());

            CoCustomer customer = customerRepository.findByPhoneNumber(mobileNumber).orElseGet(() -> createCustomer(mobileNumber));

            MDC.put("customerId", customer.getCustomerId() == null ? "-" : String.valueOf(customer.getCustomerId()));

            log.debug("OTP_SERVICE | RESEND_OTP | resolvedCustomer");

            validateCustomerStatus(customer);
            validateOtpRateLimit(customer.getPhoneNumber());

            lockKey = getOtpLockKey(customer.getPhoneNumber());
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(5));

            if (!Boolean.TRUE.equals(locked)) {
                log.warn("OTP_SERVICE | RESEND_OTP | LOCK_ACQUIRE_FAILED");
                throw new CoBadRequestException("OTP generation already in progress");
            }

            CustomerOtp latestOtp = customerOtpRepository.findTopByCustomerOrderByCustomerOtpIdDesc(customer).orElseThrow(() -> new OtpNotFoundException("OTP not found"));
            validateResendCooldown(latestOtp);

            Integer resendCount = latestOtp.getResendCount() == null ? 0 : latestOtp.getResendCount();

            if (resendCount >= MAX_RESEND_COUNT) {
                log.warn("OTP_SERVICE | RESEND_OTP | resendLimitExceeded | resendCount={}", resendCount);
                throw new MaxOtpResendException("Maximum resend limit exceeded");
            }

            latestOtp.setIsUsed(true);
            latestOtp.setUpdatedAt(LocalDateTime.now());
            customerOtpRepository.saveAndFlush(latestOtp);

            String otp = generateOtp(); // DO NOT log OTP
            String otpHash = passwordEncoder.encode(otp);
            resendCount++;

            String referenceId = smsCountryService.sendOtp(customer.getPhoneNumber(), otp);
            log.info("OTP_SERVICE | RESEND_OTP | smsSent | referenceId={}", referenceId);

            saveOtpInRedis(customer, otpHash, resendCount);
            CustomerOtp newOtp = saveOtpTransaction(customer, otpHash, resendCount);
            newOtp.setOtpReferenceId(referenceId);
            customerOtpRepository.saveAndFlush(newOtp);

            log.info("OTP_SERVICE | RESEND_OTP | SUCCESS | newOtpId={}", newOtp.getCustomerOtpId());
            return new ApiResponseDto(true, "OTP resent successfully");
        } catch (Exception ex) {
            log.error("OTP_SERVICE | RESEND_OTP | ERROR | message={}", ex.getMessage(), ex);
            throw ex;
        } finally {
            if (lockKey != null) {
                redisTemplate.delete(lockKey);
                log.debug("OTP_SERVICE | RESEND_OTP | lockReleased");
            }
            MDC.remove("operation");
            MDC.remove("mobile");
            MDC.remove("customerId");
        }
    }

    private String generateOtp() {

        SecureRandom random = new SecureRandom();

        int otp = 100000 + random.nextInt(900000);

        return String.valueOf(otp);
    }

    private String getRedisKey(Integer customerId) {

        return "OTP:" + customerId;
    }

    private String getOtpLockKey(String mobileNumber) {

        return "OTP_LOCK:" + mobileNumber;
    }

    private String getOtpRateLimitKey(String mobileNumber) {

        return "OTP_RATE:" + mobileNumber;
    }

    private void saveOtpInRedis(CoCustomer customer, String otpHash, Integer resendCount) {

        OtpCacheDto cache = new OtpCacheDto();

        cache.setCustomerId(customer.getCustomerId().longValue());

        cache.setOtpHash(otpHash);

        cache.setRetryCount(0);

        cache.setResendCount(resendCount);

        redisTemplate.opsForValue().set(getRedisKey(customer.getCustomerId()), cache, Duration.ofMinutes(OTP_EXPIRY_MINUTES));
    }

    private CustomerOtp saveOtpTransaction(CoCustomer customer, String otpHash, Integer resendCount) {

        CustomerOtp otp = new CustomerOtp();

        otp.setCustomer(customer);

        otp.setOtpHash(otpHash);

        otp.setRetryCount(0);

        otp.setResendCount(resendCount);

        otp.setIsVerified(false);

        otp.setIsUsed(false);

        otp.setCreatedAt(LocalDateTime.now());

        otp.setCreatedBy(1);

        otp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));


        CustomerOtp saved = customerOtpRepository.saveAndFlush(otp);

        log.info("OTP SAVED SUCCESSFULLY otpId={} customerId={}", saved.getCustomerOtpId(), customer.getCustomerId());

        return saved;
    }

    private void validateOtp(CustomerOtp otp) {

        if (Boolean.TRUE.equals(otp.getIsUsed())) {
            log.warn("OTP_SERVICE | VALIDATE_OTP | otpId={} | alreadyUsed", otp.getCustomerOtpId());
            throw new OtpAlreadyUsedException("OTP already used");
        }
        if (otp.getExpiresAt() == null) {

            log.error("OTP_SERVICE | VALIDATE_OTP | otpId={} | expiryMissing", otp.getCustomerOtpId());

            throw new OtpExpiredException("OTP expiry information missing");
        }
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("OTP_SERVICE | VALIDATE_OTP | otpId={} | expired at {}", otp.getCustomerOtpId(), otp.getExpiresAt());
            otp.setIsUsed(true);
            otp.setUpdatedAt(LocalDateTime.now());
            customerOtpRepository.save(otp);
            throw new OtpExpiredException("OTP expired");
        }

        Integer retryCount = otp.getRetryCount() == null ? 0 : otp.getRetryCount();
        if (retryCount >= MAX_RETRY_COUNT) {
            log.warn("OTP_SERVICE | VALIDATE_OTP | otpId={} | maxRetryExceeded | retryCount={}", otp.getCustomerOtpId(), retryCount);
            throw new MaxOtpRetryException("Maximum retry exceeded");
        }
    }

    private void handleInvalidOtp(CustomerOtp otp, OtpCacheDto cache, String redisKey) {

        Integer retryCount = otp.getRetryCount() == null ? 0 : otp.getRetryCount();
        retryCount++;
        otp.setRetryCount(retryCount);
        cache.setRetryCount(retryCount);

        if (retryCount >= MAX_RETRY_COUNT) {
            log.warn("OTP_SERVICE | HANDLE_INVALID_OTP | otpId={} | maxRetryReached | markingUsed", otp.getCustomerOtpId());
            otp.setIsUsed(true);
            redisTemplate.delete(redisKey);
        } else {
            log.debug("OTP_SERVICE | HANDLE_INVALID_OTP | otpId={} | retryCount={} | updating cache", otp.getCustomerOtpId(), retryCount);
            redisTemplate.opsForValue().set(redisKey, cache, Duration.ofMinutes(OTP_EXPIRY_MINUTES));
        }

        otp.setUpdatedAt(LocalDateTime.now());
        customerOtpRepository.save(otp);
    }

    private void invalidateOldOtp(CoCustomer customer) {

        customerOtpRepository.findTopByCustomerAndIsUsedFalseOrderByCustomerOtpIdDesc(customer).ifPresent(otp -> {
            log.debug("OTP_SERVICE | INVALIDATE_OLD_OTP | customerId={} | otpId={} | invalidating", customer.getCustomerId(), otp.getCustomerOtpId());
            otp.setIsUsed(true);
            otp.setUpdatedAt(LocalDateTime.now());
            customerOtpRepository.save(otp);
            redisTemplate.delete(getRedisKey(customer.getCustomerId()));
            log.debug("OTP_SERVICE | INVALIDATE_OLD_OTP | customerId={} | cacheCleared", customer.getCustomerId());
        });
    }

    private void validateDailyLimit(CoCustomer customer) {

        LocalDate today = LocalDate.now();

        long count = customerOtpRepository.countByCustomerAndCreatedAtBetween(customer, today.atStartOfDay(), today.plusDays(1).atStartOfDay());

        if (count >= DAILY_OTP_LIMIT) {
            log.warn("OTP_SERVICE | DAILY_LIMIT | customerId={} | todaysCount={} | limit={}", customer.getCustomerId(), count, DAILY_OTP_LIMIT);
            throw new MaxOtpRetryException("Daily OTP limit exceeded");
        }
    }

    private void validateCustomerStatus(CoCustomer customer) {

        if (customer.getCustomerStatus() == null) {

            log.warn("OTP_SERVICE | CUSTOMER_STATUS | customerId={} | status=NULL", customer.getCustomerId());

            throw new CustomerBlockedException("Customer status not configured");
        }

        String status = customer.getCustomerStatus().getStatusName();

        if ("BLOCKED".equalsIgnoreCase(status) || "INACTIVE".equalsIgnoreCase(status) || "DELETED".equalsIgnoreCase(status) || "SUSPENDED".equalsIgnoreCase(status)) {

            log.warn("OTP_SERVICE | CUSTOMER_STATUS | customerId={} | status={}", customer.getCustomerId(), status);

            throw new CustomerBlockedException("Customer account is not active");
        }
    }

    private void validateOtpRateLimit(String mobileNumber) {

        String key = getOtpRateLimitKey(mobileNumber);

        Boolean allowed = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(60));

        if (!Boolean.TRUE.equals(allowed)) {
            log.warn("OTP_SERVICE | RATE_LIMIT | mobile={} | blocked for 60 seconds", mobileNumber);
            throw new CoBadRequestException("Please wait 60 seconds before requesting another OTP");
        }
    }

    private synchronized CoCustomer createCustomer(String mobileNumber) {

        log.info("OTP_SERVICE | CREATE_CUSTOMER | mobile={} | START", mobileNumber);

        CoCustomer existingCustomer = customerRepository.findByPhoneNumber(mobileNumber).orElse(null);

        if (existingCustomer != null) {

            log.info("OTP_SERVICE | CREATE_CUSTOMER | mobile={} | alreadyExists | customerId={}", mobileNumber, existingCustomer.getCustomerId());

            return existingCustomer;
        }

        CustomerStatus customerStatus = customerStatusRepository.findByStatusName("NEW").orElseThrow(() -> new CoBusinessException("Customer status NEW not found"));

        if (customerStatus.getCustomerStatusId() == null) {

            throw new CoBusinessException("Invalid customer status configuration");
        }

        CoCustomer customer = new CoCustomer();

        customer.setPhoneNumber(mobileNumber);
        customer.setFirstName(generateRandomUserName());
        customer.setCustomerStatus(customerStatus);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setCreatedBy(1);

        CoCustomer savedCustomer = customerRepository.saveAndFlush(customer);

        log.info("OTP_SERVICE | CREATE_CUSTOMER | mobile={} | customerId={} | SUCCESS", savedCustomer.getPhoneNumber(), savedCustomer.getCustomerId());

        return savedCustomer;
    }

    private String generateRandomUserName() {

        SecureRandom random = new SecureRandom();

        return "User" + (System.currentTimeMillis() % 100000) + random.nextInt(100);
    }

    private String normalizeMobile(String mobileNumber) {

        if (mobileNumber == null) {
            throw new CoBadRequestException("Mobile number is required");
        }

        mobileNumber = mobileNumber.replaceAll("\\D", "");

        if (mobileNumber.startsWith("91") && mobileNumber.length() == 12) {

            mobileNumber = mobileNumber.substring(2);
        }

        if (!mobileNumber.matches("\\d{10}")) {
            throw new CoBadRequestException("Invalid mobile number");
        }

        return mobileNumber;
    }

    private void validateOtpFormat(String otp) {

        if (otp == null || otp.isBlank()) {

            throw new InvalidOtpException("OTP is required");
        }

        if (!otp.matches("\\d{6}")) {

            throw new InvalidOtpException("OTP must be 6 digits");
        }
    }

    private void validateResendCooldown(CustomerOtp otp) {

        if (otp.getCreatedAt() == null) {
            return;
        }

        if (otp.getCreatedAt().plusSeconds(30).isAfter(LocalDateTime.now())) {

            throw new CoBadRequestException("Please wait 30 seconds before requesting another OTP");
        }
    }

    private void validateOtpCache(OtpCacheDto cache) {

        if (cache == null) {

            throw new OtpExpiredException("OTP session expired");
        }

        if (cache.getCustomerId() == null) {

            throw new OtpExpiredException("Invalid OTP session");
        }

        if (cache.getOtpHash() == null || cache.getOtpHash().isBlank()) {

            throw new OtpExpiredException("OTP session corrupted");
        }
    }
}
