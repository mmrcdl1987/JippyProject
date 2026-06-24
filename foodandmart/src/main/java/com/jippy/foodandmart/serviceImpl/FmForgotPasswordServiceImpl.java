package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.Enum.FmUserType;
import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmEmployee;
import com.jippy.foodandmart.entity.FmMerchant;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.entity.FmUser;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.feignClients.DivisionFeignClient;
import com.jippy.foodandmart.feignClients.DriverFeignClient;
import com.jippy.foodandmart.repository.FmEmployeeRepository;
import com.jippy.foodandmart.repository.FmMerchantRepository;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.repository.FmUserRepository;
import com.jippy.foodandmart.service.FmForgotPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmForgotPasswordServiceImpl implements FmForgotPasswordService {

    // Repository used to validate employee email
    private final FmEmployeeRepository employeeRepository;

    // Repository used to validate merchant email
    private final FmMerchantRepository merchantRepository;

    // Repository used to validate outlet email
    private final FmOutletRepository outletRepository;

    // Feign client used to fetch driver details from Driver Microservice
    private final DriverFeignClient fmDriverFeignClient;

    // Redis used to store OTP temporarily with expiry time
    private final RedisTemplate<String, String> redisTemplate;

    // Feign client used to send OTP mail through Division Microservice
    private final DivisionFeignClient divisionFeignClient;

    private final FmUserRepository usersRepository;

//     to encrypt the password in to the users table
    private final PasswordEncoder passwordEncoder;

    @Override
    public FmForgotPasswordResponseDto forgetPasswordForUserTypeBySendingOtpToMail(
            FmForgotPasswordOtpRequestDto requestDto) {

        log.info("Forgot Password OTP request received for email : {}", requestDto.getEmail());

        // 1.Remove leading and trailing spaces from email
        String email = requestDto.getEmail().trim();

        // 2.Convert user type string into Enum
        FmUserType userType =
                FmUserType.valueOf(requestDto.getUserType().toUpperCase());

        //    calling helper method for finding Email for Particular UserType in those tables
        Integer userId = getUserIdByEmailAndUserType(email,userType);

        // Generate random 6-digit OTP
        String otp = generateOtp();

        // Create unique Redis key using UserType and UserId
        // Example:
        // FORGOT_PASSWORD_OTP_DRIVER_44
        String redisKey =
                FmAppConstants.FORGOT_PASSWORD_OTP + userType + "_" + userId;

        // Store OTP in Redis using UserType and UserId as the key.
        //
        // Example:
        // Key   : FORGOT_PASSWORD_OTP_DRIVER_44
        // Value : 654321(OTP)
        // Store generated OTP in Redis for 5 minutes.
        // Example: FORGOT_PASSWORD_OTP_DRIVER_44 = 654321
        // OTP will expire/deleted automatically after 5 minutes IN REDIS.
        redisTemplate.opsForValue()
                .set(redisKey, otp, 5, TimeUnit.MINUTES);

        log.info("OTP stored successfully in Redis with key : {}", redisKey);

//       Just for checking printing logs what actually stored in REDIS Locally
        Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);

        log.info("Redis Key : {}", redisKey);

        log.info("Redis Value(otp) : {}", otp);

        log.info("Redis TTL expiry in  {} seconds :", ttl);



        // Prepare request object for Division Microservice
        FmSendOtpMailRequestDto mailRequestDto =  new FmSendOtpMailRequestDto();

        mailRequestDto.setEmail(email);

        mailRequestDto.setOtp(otp);

        // Call Division Microservice to send OTP mail
        divisionFeignClient.sendOtpMail(mailRequestDto);

        log.info("OTP mail sent successfully to : {}", email);

        FmForgotPasswordResponseDto otpSentSuccessfully =
                new FmForgotPasswordResponseDto(
                true,
                "OTP sent successfully");

        // Return success response
        return otpSentSuccessfully;
    }

    /**
     * Generates a random 6-digit OTP.
     * Example: 458721
     */
    private String generateOtp() {

        Random random = new Random();

        int otp = 100000 + random.nextInt(900000);

        return String.valueOf(otp);
    }

//  for validating Forget Password from redis
@Override
public FmForgotPasswordResponseDto validateForgotPasswordOtp(
        FmValidateForgotPasswordOtpRequestDto requestDto) {

    log.info("OTP validation request received for email : {}",
            requestDto.getEmail());

    // Remove unnecessary spaces from email
    // Example:
    // "  jippy@gmail.com  " -> "jippy@gmail.com"
    String email = requestDto.getEmail().trim();

    // Convert incoming user type into Enum
    // Example:
    // DRIVER -> FmUserType.DRIVER
    // driver -> FmUserType.DRIVER
    FmUserType userType =
            FmUserType.valueOf(requestDto.getUserType().toUpperCase());

    //    calling helper method for finding Email for Particular UserType in those tables
    Integer userId=getUserIdByEmailAndUserType(email,userType);

    // Build the same Redis key which was used while storing OTP.
    //
    // Example during OTP generation:
    // UserType = DRIVER
    // DriverId = 44
    //
    // Redis Key Stored:
    // FORGOT_PASSWORD_OTP_DRIVER_44
    //
    // We must generate the same key again to fetch OTP from Redis.
    String redisKey =
            FmAppConstants.FORGOT_PASSWORD_OTP + userType + "_" + userId;

    // Fetch/getting OTP from Redis using generated rediskey(samekey-FORGOT_PASSWORD_OTP_DRIVER_44).
    //
    // Example:
    // Redis contains:
    // FORGOT_PASSWORD_OTP_DRIVER_44 = 654321
    //
    // storedOtp = 654321
    String storedOtp =
            redisTemplate.opsForValue().get(redisKey);

    log.info("Stored OTP from Redis : {}", storedOtp);

    // If Redis returns null,
    // it means OTP is not available anymore.
    //
    // Possible reasons:
    // 1. OTP expired after 5 minutes.
    // 2. OTP was already validated and deleted.
    // 3. OTP was never generated.
    if (storedOtp == null) {

        return new FmForgotPasswordResponseDto(
                false,
                "OTP expired. Please request a new OTP");
    }

    // Compare Redis OTP with user entered OTP.
    //
    // Example:
    // Redis OTP = 654321
    // User OTP  = 111111
    //
    // Since both values are different,
    // validation fails.
    if (!storedOtp.equals(requestDto.getOtp())) {

        return new FmForgotPasswordResponseDto(
                false,
                "Invalid OTP. Please try again");
    }// else storedOtp.equals(requestDto.getOtp())

    // OTP matched successfully.
    //
    // Example:
    // Redis OTP = 654321
    // User OTP  = 654321
    //
    // Since OTP is valid,
    // remove it from Redis immediately.
    //
    // This prevents OTP reuse and makes it
    // a true One-Time Password.
    redisTemplate.delete(redisKey);

    log.info("OTP validated successfully for key : {}", redisKey);

    // Return success response
    FmForgotPasswordResponseDto otpValidatedSuccessfully =
            new FmForgotPasswordResponseDto(
            true,
            "OTP validated successfully");


    return otpValidatedSuccessfully;

}

//-----------------------------------------------------------------------------------------------
    /** HELPER METHOD
     * Fetches the primary key of the user based on Email and User Type.
     * This User ID is used while generating Redis keys for OTP operations.
     */
//    HELPER METHOD
    private Integer getUserIdByEmailAndUserType(
            String email,
            FmUserType userType) {

        // Variable used to store primary key of the user
        Integer userId = null;

        // Validate email based on user type and fetch corresponding primary key
        switch (userType) {

            case EMPLOYEE:

                // Verify employee email exists
                FmEmployee employee = employeeRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employee email not found"));

                // Store Employee ID
                userId = employee.getEmployeeId();

                break;

            case MERCHANT:

                // Verify merchant email exists
                FmMerchant merchant = merchantRepository
                        .findByMerchantEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Merchant email not found"));

                // Store Merchant ID
                userId = merchant.getMerchantId();

                break;

            case OUTLET:

                // Verify outlet email exists
                FmOutlet outlet = outletRepository
                        .findByOutletEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Outlet email not found"));

                // Store Outlet ID
                userId = outlet.getOutletId();

                break;

            case DRIVER:

                // Verify driver email through Driver Microservice
                FmDriverDto driver =
                        fmDriverFeignClient.findByEmail(email);

                if (driver == null) {
                    throw new ResourceNotFoundException(
                            "Driver email not found");
                }

                // Store Driver ID
                userId = driver.getDriverId();

                break;

            default:

                // Invalid user type received
                throw new ResourceNotFoundException(
                        "Invalid user type");
        }

        // Return primary key of the respective user
        return userId;
    }
/*------------------------------------------------------------------*/
@Override
public FmForgotPasswordResponseDto updateForgotPassword(
        FmUpdateForgotPasswordRequestDto requestDto) {

    log.info("Update password request received for email : {}", requestDto.getEmail());

    String email = requestDto.getEmail().trim();

    FmUserType userType = FmUserType.valueOf(requestDto.getUserType().toUpperCase());

    /*
     * Find primary key using email and role.
     *
     * Example:
     * EMPLOYEE -> employee_id = 7
     * MERCHANT -> merchant_id = 32
     * DRIVER   -> driver_id = 44
     */
    Integer userId = getUserIdByEmailAndUserType(email, userType);

    /*
     * Fetch record from users table
     *
     * SELECT * FROM users
     * WHERE user_id = 7(users table)
     * AND user_type = 'EMPLOYEE'
     */
    FmUser user = usersRepository.findByUserIdAndUserType(userId, userType.name());
    if (user == null) {
        throw new ResourceNotFoundException("User not found with userId : " + userId);
    }
    /*
     * Encrypt password using BCrypt
     *-----------------------------------
     * Example:
     Rohan@123 becomes  $2a$10$Kx..... */
        String encryptedPassword = passwordEncoder.encode(requestDto.getNewPassword());

        user.setPassword(encryptedPassword);

        usersRepository.save(user);

        log.info("Password updated successfully for userId : {}", userId);

    FmForgotPasswordResponseDto passwordUpdatedSuccessfully =
                new FmForgotPasswordResponseDto(
                true,
                "Password updated successfully");

        return passwordUpdatedSuccessfully;
    }
}