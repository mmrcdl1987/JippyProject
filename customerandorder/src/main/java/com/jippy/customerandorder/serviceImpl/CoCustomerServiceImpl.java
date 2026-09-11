package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.*;
import com.jippy.customerandorder.exception.CoBadRequestException;
import com.jippy.customerandorder.exception.CoBusinessException;
import com.jippy.customerandorder.exception.CoResourceNotFoundException;
import com.jippy.customerandorder.feignClients.DivisionFeignClient;
import com.jippy.customerandorder.feignClients.DriverFeignClient;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.feignClients.NotificationFeignClient;
import com.jippy.customerandorder.iservice.ICoCustomerService;
import com.jippy.customerandorder.mapper.CoCustomerMapper;
import com.jippy.customerandorder.mapper.CoOrderCompleteDetailsMapper;
import com.jippy.customerandorder.mapper.CoWalletPointsMapper;
import com.jippy.customerandorder.producer.CoWalletPointsKafkaProducer;
import com.jippy.customerandorder.projection.*;
import com.jippy.customerandorder.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoCustomerServiceImpl implements ICoCustomerService {
    private final CoOrderRepository coOrderRepository;
    private final CoOrderPriceBreakupRepository coOrderPriceBreakupRepository;
    private final CoOrderItemRepository coOrderItemRepository;

    private final CoCustomerRepository customerRepository;

    private final CoCustomerWalletRepository walletRepository;

    private final CoWalletSettingsRepository walletSettingsRepository;

    private final CoCustomerWalletTransactionsRepository transactionsRepository;

    private final CoCustomerStreakRepository streakRepository;

    private final CoCustomerMapper customerMapper;

    private final CoCustomerReferralRepository customerReferralRepository;

    private final FMFeignClient fmFeignClient;

    private final DriverFeignClient driverFeignClient;

    private final DivisionFeignClient divisionFeignClient;

    private final NotificationFeignClient notificationFeignClient;

    private final S3ImageService s3ImageService;

    private final CoWalletPointsKafkaProducer walletPointsKafkaProducer;


    // QUALIFY REFERRAL ON FIRST ORDER
    @Override
    @Transactional
    public void qualifyReferralOnFirstOrder(Integer customerId) {
        log.info("REFERRAL_QUALIFICATION_START | customerId={}", customerId);

        // Check if customer has a pending referral
        Optional<CoCustomerReferral> referralOpt = customerReferralRepository.findByRefereeCustomerIdAndReferralStatus(customerId, COConstants.REFERRAL_STATUS[0]);

        if (referralOpt.isEmpty()) {
            log.info("REFERRAL_NO_PENDING | customerId={}", customerId);
            return;
        }

        CoCustomerReferral referral = referralOpt.get();

        try {
            // Update referral status to qualified
            referral.setReferralStatus(COConstants.REFERRAL_STATUS[1]);
            referral.setUpdatedAt(LocalDateTime.now());
            referral.setUpdatedBy(1);
            customerReferralRepository.save(referral);

            log.info("REFERRAL_QUALIFIED_SUCCESS | referralId={} | referrerId={} | refereeCustomerId={}", referral.getReferralId(), referral.getReferrerCustomerId(), customerId);

        } catch (Exception ex) {
            log.error("REFERRAL_QUALIFICATION_FAILED | customerId={} | error={}", customerId, ex.getMessage(), ex);
            // Don't fail order placement if referral qualification fails
        }
    }

    // PROCESS REFERRAL REWARD
    @Override
    @Transactional
    public void processReferralReward(Integer customerId, String orderId) {
        log.info("REFERRAL_REWARD_PROCESS_START | customerId={} | orderId={}", customerId, orderId);

        // Check if customer has a pending referral reward
        Optional<CoCustomerReferral> referralOpt = customerReferralRepository.findByRefereeCustomerIdAndReferralStatus(customerId, COConstants.REFERRAL_STATUS[1]);

        if (referralOpt.isEmpty()) {
            log.info("REFERRAL_REWARD_NO_QUALIFIED | customerId={}", customerId);
            return;
        }

        CoCustomerReferral referral = referralOpt.get();

        try {
            // Get referrer's wallet
            CoCustomerWallet referrerWallet = walletRepository.findByCustomerCustomerId(referral.getReferrerCustomerId()).orElseThrow(() -> new CoBusinessException(COConstants.WALLET_NOT_FOUND));

            CoWalletSettings referralSettings = walletSettingsRepository.findBySettingType(COConstants.REFERRAL_REWARD_POINTS).orElseThrow(() -> new CoBusinessException("Referral reward points not configured"));

            Integer referralPoints = referralSettings.getSettingValue();

            // Add 250 points to referrer's wallet
            Integer currentBalance = referrerWallet.getBalancePoints() != null ? referrerWallet.getBalancePoints() : 0;
            referrerWallet.setBalancePoints(currentBalance + referralPoints);
            referrerWallet.setUpdatedAt(LocalDateTime.now());
            referrerWallet.setUpdatedBy(1); // System user
            walletRepository.save(referrerWallet);

            log.info("REFERRAL_REWARD_WALLET_UPDATED | referrerId={} | pointsAdded={} | newBalance={}", referral.getReferrerCustomerId(), COConstants.REFERRAL_REWARD_POINTS, referrerWallet.getBalancePoints());

            // Record transaction
            CoCustomerWalletTransactions transaction = new CoCustomerWalletTransactions();
            transaction.setWalletId(referrerWallet.getWalletId());
            transaction.setTransactionType(COConstants.REFERRAL_REWARD_POINTS);
            transaction.setPoints(referralPoints);
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setCreatedBy(1);
            transactionsRepository.save(transaction);

            // Publish Kafka event for referral reward points notification
            CoWalletPointsEvent referralPointsEvent = CoWalletPointsMapper.toReferralPointsEvent(referral.getReferrerCustomerId(), referralPoints, orderId);
            walletPointsKafkaProducer.sendWalletPointsEvent(referralPointsEvent);
            log.info("REFERRAL_POINTS_EVENT_PUBLISHED | referrerId={} | points={} | orderId={}", referral.getReferrerCustomerId(), referralPoints, orderId);

            // Update referral record status to rewarded
            referral.setReferralStatus(COConstants.REFERRAL_STATUS[2]);
            referral.setUpdatedAt(LocalDateTime.now());
            referral.setUpdatedBy(1);
            customerReferralRepository.save(referral);

            log.info("REFERRAL_REWARD_PROCESSED_SUCCESS | referralId={} | referrerId={} | refereeCustomerId={}", referral.getReferralId(), referral.getReferrerCustomerId(), customerId);

        } catch (Exception ex) {
            log.error("REFERRAL_REWARD_PROCESS_FAILED | customerId={} | error={}", customerId, ex.getMessage(), ex);
            throw new CoBusinessException("Failed to process referral reward");
        }
    }

    // CREATE CUSTOMER
    @Override
    @Transactional
    public CoCustomer createCustomer(CoCustomerRequestDto dto) {

        log.info("CUSTOMER_REGISTRATION_COMPLETION_START | customerId={}", dto.getCustomerId());

        // ==========================================
        // GET EXISTING VERIFIED CUSTOMER
        // ==========================================

        CoCustomer customer = customerRepository.findById(dto.getCustomerId()).orElseThrow(() -> {

            log.error("CUSTOMER_NOT_FOUND | customerId={}", dto.getCustomerId());

            return new CoBadRequestException(COConstants.MSG_CUSTOMER_NOT_FOUND);
        });

        // ==========================================
        // VERIFY PHONE NUMBER
        // ==========================================

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {

            String requestPhone = dto.getPhoneNumber().trim();
            String verifiedPhone = customer.getPhoneNumber();

            if (verifiedPhone == null || !requestPhone.equals(verifiedPhone)) {

                log.warn("PHONE_NUMBER_MISMATCH | customerId={} | " + "verifiedPhone={} | requestPhone={}", customer.getCustomerId(), verifiedPhone, requestPhone);
                throw new CoBadRequestException("Phone number mismatch");
            }

            log.info("PHONE_NUMBER_VERIFIED | customerId={} | phone={}", customer.getCustomerId(), requestPhone);
        }

        // ==========================================
        // CHECK EMAIL
        // ==========================================

        if (dto.getEmail().isBlank()) {
            throw new CoBadRequestException("Email can not be blank");
        } else {
            Optional<CoCustomer> existingEmail = customerRepository.findByEmail(dto.getEmail().trim());

            if (existingEmail.isPresent() && !existingEmail.get().getCustomerId().equals(customer.getCustomerId())) {

                log.error("EMAIL_ALREADY_EXISTS | email={} | existingCustomerId={}", dto.getEmail(), existingEmail.get().getCustomerId());
                throw new CoBadRequestException(COConstants.EMAIL_ALREADY_EXISTS);
            }
        }

        // ==========================================
        // UPDATE EXISTING CUSTOMER
        // ==========================================

        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setDateOfBirth(dto.getDOB());

        // Generate referral code for this customer
        if (customer.getReferralCode() == null || customer.getReferralCode().isBlank()) {

            String referralCode = CoCustomerMapper.generateReferral(dto.getFirstName(), dto.getLastName(), dto.getPhoneNumber());

            customer.setReferralCode(referralCode);

            log.info("REFERRAL_CODE_GENERATED | customerId={} | referralCode={}", customer.getCustomerId(), referralCode);
        }

        customer.setUpdatedAt(LocalDateTime.now());
        customer.setUpdatedBy(dto.getCreatedBy());

        CoCustomer savedCustomer = customerRepository.save(customer);

        log.info("CUSTOMER_UPDATED | customerId={}", savedCustomer.getCustomerId());

        // ==========================================
        // SAVE FCM TOKEN IN NOTIFICATION SERVICE
        // ==========================================

        if (dto.getFcmToken() != null && !dto.getFcmToken().isBlank()) {

            try {

                CoDeviceTokenRequestDto deviceTokenRequest = new CoDeviceTokenRequestDto();
                deviceTokenRequest.setUserId(savedCustomer.getCustomerId());
                deviceTokenRequest.setUserType("CUSTOMER");
                deviceTokenRequest.setDeviceType("ANDROID");
                deviceTokenRequest.setFcmToken(dto.getFcmToken().trim());

                notificationFeignClient.saveDeviceToken(deviceTokenRequest);

                log.info("FCM_TOKEN_SAVED_IN_NOTIFICATION_SERVICE | customerId={}", savedCustomer.getCustomerId());

            } catch (Exception ex) {

                log.error("FCM_TOKEN_SAVE_FAILED | customerId={} | error={}", savedCustomer.getCustomerId(), ex.getMessage(), ex);
            }
        }

        // ==========================================
        // PROCESS REFERRAL
        // ==========================================

        if (dto.getReferralCodeUsed() != null && !dto.getReferralCodeUsed().isBlank()) {
            String referralCode = dto.getReferralCodeUsed().trim();

            try {
                CoCustomer referrer = customerRepository.findByReferralCode(referralCode).orElseThrow(() -> new CoBadRequestException("Invalid referral code"));
                // Prevent self referral
                if (referrer.getCustomerId().equals(savedCustomer.getCustomerId())) {

                    throw new CoBadRequestException("Customer cannot use own referral code");
                }

                Optional<CoCustomerReferral> existingReferral = customerReferralRepository.findByRefereeCustomerId(savedCustomer.getCustomerId());

                if (existingReferral.isPresent()) {

                    log.info("REFERRAL_ALREADY_EXISTS | customerId={} | referralId={}", savedCustomer.getCustomerId(), existingReferral.get().getReferralId());

                } else {

                    // --------------------------------
                    // Create referral record
                    // --------------------------------

                    CoCustomerReferral referral = new CoCustomerReferral();

                    referral.setReferrerCustomerId(referrer.getCustomerId());
                    referral.setRefereeCustomerId(savedCustomer.getCustomerId());
                    referral.setReferralCode(referralCode);
                    referral.setReferralStatus(COConstants.REFERRAL_STATUS[0]);
                    referral.setReferralType("customer");
                    referral.setCreatedAt(LocalDateTime.now());
                    referral.setCreatedBy(dto.getCreatedBy());
                    customerReferralRepository.save(referral);
                    savedCustomer.setUsedReferral(referralCode);
                    customerRepository.save(savedCustomer);
                    log.info("REFERRAL_CREATED | referralId={} | " + "referrerId={} | refereeId={}", referral.getReferralId(), referrer.getCustomerId(), savedCustomer.getCustomerId());
                }
            } catch (CoBadRequestException ex) {
                log.error("REFERRAL_FAILED | customerId={} | " + "referralCode={} | error={}", savedCustomer.getCustomerId(), referralCode, ex.getMessage());
                throw ex;
            }
        }

        // ==========================================
        // CREATE WALLET IF NOT EXISTS
        // ==========================================

        Optional<CoCustomerWallet> existingWallet = walletRepository.findByCustomerCustomerId(savedCustomer.getCustomerId());

        if (existingWallet.isEmpty()) {
            log.info("WALLET_NOT_FOUND | customerId={} | creating wallet", savedCustomer.getCustomerId());

            CoWalletSettings walletSettings = walletSettingsRepository.findBySettingType(COConstants.WELCOME_POINTS).orElseThrow(() -> new CoBusinessException(COConstants.WELCOME_POINTS_NOT_CONFIGURED));

            CoCustomerWallet wallet = CoCustomerMapper.mapToWallet(savedCustomer, walletSettings.getSettingValue(), dto.getCreatedBy());

            CoCustomerWallet savedWallet = walletRepository.save(wallet);

            log.info("WALLET_CREATED | customerId={} | walletId={} | points={}", savedCustomer.getCustomerId(), savedWallet.getWalletId(), savedWallet.getBalancePoints());

            // ==========================================
            // WELCOME TRANSACTION
            // ==========================================

            CoCustomerWalletTransactions transaction = new CoCustomerWalletTransactions();
            transaction.setWalletId(savedWallet.getWalletId());
            transaction.setTransactionType(COConstants.WELCOME_POINTS);
            transaction.setPoints(walletSettings.getSettingValue());
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setCreatedBy(dto.getCreatedBy());
            transactionsRepository.save(transaction);
            log.info("WELCOME_TRANSACTION_CREATED | " + "customerId={} | walletId={} | points={}", savedCustomer.getCustomerId(), savedWallet.getWalletId(), walletSettings.getSettingValue());

            // Publish Kafka event for welcome points notification
            CoWalletPointsEvent welcomePointsEvent = CoWalletPointsMapper.toWelcomePointsEvent(savedCustomer.getCustomerId(), walletSettings.getSettingValue(), dto.getFcmToken());
            walletPointsKafkaProducer.sendWalletPointsEvent(welcomePointsEvent);
            log.info("WELCOME_POINTS_EVENT_PUBLISHED | customerId={} | points={}", savedCustomer.getCustomerId(), walletSettings.getSettingValue());

        } else {

            log.info("WALLET_ALREADY_EXISTS | customerId={} | walletId={}", savedCustomer.getCustomerId(), existingWallet.get().getWalletId());
        }

        log.info("CUSTOMER_REGISTRATION_COMPLETION_SUCCESS | customerId={}", savedCustomer.getCustomerId());

        return savedCustomer;
    }

    //===============
    // CONVERT POINTS
    //===============


    @Override
    public CoWalletResponseDto convertPoints(Integer customerId) {
        log.info("Point conversion started");

        // FETCH WALLET

        CoCustomerWallet wallet = walletRepository.findByCustomerCustomerId(customerId).orElseThrow(() -> {
            log.error("Wallet not found");
            return new CoBusinessException(COConstants.WALLET_NOT_FOUND);
        });

        Integer balancePoints = wallet.getBalancePoints();
        log.info("Current balance points : {}", balancePoints);

        // MINIMUM VALIDATION

        if (balancePoints == null || balancePoints < COConstants.MINIMUM_POINTS_REQUIRED) {
            log.warn("Minimum points not reached");
            CoWalletResponseDto response = new CoWalletResponseDto();
            response.setSuccess(true);
            response.setMessage(COConstants.MINIMUM_POINTS_REQUIRED_MESSAGE);
            response.setWalletId(wallet.getWalletId());
            response.setCustomerId(wallet.getCustomer().getCustomerId());
            response.setBalancePoints(wallet.getBalancePoints());
            response.setBalanceAmount(wallet.getBalanceAmount());

            return response;
        }

        // CONVERSION LOGIC

        int eligibleBlocks = balancePoints / COConstants.MINIMUM_POINTS_REQUIRED;
        int remainingPoints = balancePoints % COConstants.MINIMUM_POINTS_REQUIRED;
        BigDecimal convertedAmount = BigDecimal.valueOf(eligibleBlocks * COConstants.AMOUNT_PER_1000_POINTS);
        BigDecimal existingAmount = wallet.getBalanceAmount() != null ? wallet.getBalanceAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = existingAmount.add(convertedAmount);
        log.info("Eligible blocks : {}", eligibleBlocks);
        log.info("Remaining points : {}", remainingPoints);
        log.info("Converted amount : {}", convertedAmount);

        // UPDATE WALLET

        wallet.setBalancePoints(remainingPoints);
        wallet.setBalanceAmount(finalAmount);
        wallet.setUpdatedAt(LocalDateTime.now());
        wallet.setUpdatedBy(1);
        walletRepository.save(wallet);
        log.info("Wallet updated successfully");

        // SAVE CONVERT TRANSACTION
        CoCustomerWalletTransactions transaction = new CoCustomerWalletTransactions();
        transaction.setWalletId(wallet.getWalletId());
        transaction.setTransactionType(COConstants.POINTS_CONVERTED);
        transaction.setPoints(-(eligibleBlocks * COConstants.MINIMUM_POINTS_REQUIRED));
        transaction.setAmount(convertedAmount);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setCreatedBy(1);
        CoCustomerWalletTransactions savedTransaction = transactionsRepository.save(transaction);
        log.info("Points conversion transaction saved");

        // PUBLISH CONVERSION NOTIFICATION EVENT
        CoWalletPointsEvent conversionEvent = CoWalletPointsMapper.toPointsConvertedEvent(customerId, eligibleBlocks * COConstants.MINIMUM_POINTS_REQUIRED, convertedAmount, "CONVERT-" + customerId + "-" + savedTransaction.getCustomerWalletTransactionsId());
        conversionEvent.setFcmToken(null);
        walletPointsKafkaProducer.sendWalletPointsEvent(conversionEvent);
        log.info("POINTS_CONVERTED_EVENT_PUBLISHED | customerId={} | transactionId={} | amount={}", customerId, savedTransaction.getCustomerWalletTransactionsId(), convertedAmount);

        // RESPONSE

        CoWalletResponseDto response = new CoWalletResponseDto();
        response.setSuccess(true);
        response.setMessage(COConstants.POINTS_CONVERTED_SUCCESS);
        response.setWalletId(wallet.getWalletId());
        response.setCustomerId(wallet.getCustomer().getCustomerId());
        response.setBalancePoints(wallet.getBalancePoints());
        response.setBalanceAmount(wallet.getBalanceAmount());
        return response;
    }

    @Override
    public CoCustomerStreakResponseDto updateDailyStreak(Integer customerId, LocalDate date) {

        log.info("Daily streak started");

        LocalDate today = date != null ? date : LocalDate.now();

        // PREVENT SAME DAY DUPLICATE ENTRY

        boolean alreadyCheckedIn = streakRepository.existsByCustomerIdAndCheckInDate(customerId, today);

        if (alreadyCheckedIn) {

            throw new CoBusinessException("Today's streak already added");
        }

        // FETCH LAST STREAK

        CoCustomerStreak lastStreak = streakRepository.findTopByCustomerIdOrderByCheckInDateDesc(customerId).orElse(null);

        // FETCH SETTINGS

        CoWalletSettings streakSettings = walletSettingsRepository.findBySettingType(COConstants.DAILY_STREAK_POINTS).orElseThrow(() -> new CoBusinessException(COConstants.STREAK_SETTINGS_NOT_FOUND));

        CoWalletSettings streakDaySettings = walletSettingsRepository.findBySettingType(COConstants.MINIMUM_STREAK_DAYS).orElseThrow(() -> new CoBusinessException(COConstants.STREAK_SETTINGS_NOT_FOUND));

        // DAILY POINTS

        Integer streakPoints = streakSettings.getSettingValue();

        // MINIMUM STREAK DAYS

        Integer streakDays = streakDaySettings.getSettingValue();

        Integer currentStreak;

        Integer totalPoints;

        // FIRST LOGIN

        if (lastStreak == null) {

            currentStreak = 1;

            totalPoints = streakPoints;

            log.info("First streak created");

        } else {

            // CONTINUOUS LOGIN

            if (lastStreak.getCheckInDate().plusDays(1).equals(today)) {

                // RESTART AFTER REACHING STREAK LIMIT

                if (lastStreak.getCurrentStreak() >= streakDays) {

                    currentStreak = 1;

                    totalPoints = streakPoints;

                    log.info("New streak cycle started");

                } else {

                    // CONTINUE STREAK

                    currentStreak = lastStreak.getCurrentStreak() + 1;

                    /*
                     * DAY 1 = 25
                     * DAY 2 = 50
                     * DAY 3 = 75
                     */

                    totalPoints = currentStreak * streakPoints;

                    log.info("Streak continued");
                }

            } else {

                // MISSED LOGIN -> RESTART

                currentStreak = 1;

                totalPoints = streakPoints;

                log.info("Streak restarted");
            }
        }

        // SAVE DAILY STREAK

        CoCustomerStreak streak = CoCustomerMapper.mapToCustomerStreak(customerId, today, currentStreak, totalPoints, 1);

        streak = streakRepository.save(streak);

        log.info("Daily streak saved");

        // STREAK COMPLETED

        if (currentStreak == streakDays) {

            CoCustomerWallet wallet = walletRepository.findByCustomerCustomerId(customerId).orElseThrow(() -> new CoBusinessException(COConstants.WALLET_NOT_FOUND));

            Integer existingPoints = wallet.getBalancePoints() != null ? wallet.getBalancePoints() : 0;

            // ADD REWARD TO WALLET

            wallet.setBalancePoints(existingPoints + totalPoints);

            wallet.setUpdatedAt(LocalDateTime.now());

            wallet.setUpdatedBy(1);

            walletRepository.save(wallet);

            log.info("Wallet updated with streak reward");

            // SAVE TRANSACTION

            CoCustomerWalletTransactions transaction = CoCustomerMapper.mapToWalletTransaction(wallet.getWalletId(), COConstants.STREAK_REWARD, totalPoints, 1);

            transactionsRepository.save(transaction);

            log.info("Streak reward transaction saved");
        }

        // RESPONSE

        return CoCustomerMapper.mapToStreakResponse(currentStreak, totalPoints, COConstants.STREAK_UPDATED);
    }
    // WALLET POINTS TRANSFER

    @Override
    @Transactional
    public CoWalletTransferResponseDto transferWalletPoints(CoWalletTransferRequestDto requestDto) {

        log.info("Wallet points transfer started");

        // FETCH SENDER CUSTOMER

        CoCustomer senderCustomer = customerRepository.findById(requestDto.getSenderCustomerId()).orElseThrow(() -> new CoBusinessException(COConstants.CUSTOMER_NOT_FOUND));

        // FETCH RECEIVER CUSTOMER USING PHONE NUMBER

        CoCustomer receiverCustomer = customerRepository.findByPhoneNumber(requestDto.getReceiverPhoneNumber()).orElseThrow(() -> new CoBusinessException(COConstants.RECEIVER_NOT_FOUND));

        // SAME CUSTOMER VALIDATION

        if (senderCustomer.getCustomerId().equals(receiverCustomer.getCustomerId())) {

            throw new CoBusinessException(COConstants.CANNOT_TRANSFER_SELF);
        }

        // FETCH SENDER WALLET

        CoCustomerWallet senderWallet = walletRepository.findByCustomerCustomerId(senderCustomer.getCustomerId()).orElseThrow(() -> new CoBusinessException(COConstants.WALLET_NOT_FOUND));

        // FETCH RECEIVER WALLET

        CoCustomerWallet receiverWallet = walletRepository.findByCustomerCustomerId(receiverCustomer.getCustomerId()).orElseThrow(() -> new CoBusinessException(COConstants.WALLET_NOT_FOUND));

        Integer senderBalance = senderWallet.getBalancePoints() != null ? senderWallet.getBalancePoints() : 0;

        // VALIDATE BALANCE

        if (senderBalance < requestDto.getTransferPoints()) {

            throw new CoBusinessException(COConstants.INSUFFICIENT_POINTS);
        }

        // DEDUCT FROM SENDER

        senderWallet.setBalancePoints(senderBalance - requestDto.getTransferPoints());

        senderWallet.setUpdatedAt(LocalDateTime.now());

        senderWallet.setUpdatedBy(requestDto.getCreatedBy());

        walletRepository.save(senderWallet);

        log.info("Points deducted from sender wallet");

        // ADD TO RECEIVER

        Integer receiverBalance = receiverWallet.getBalancePoints() != null ? receiverWallet.getBalancePoints() : 0;

        receiverWallet.setBalancePoints(receiverBalance + requestDto.getTransferPoints());

        receiverWallet.setUpdatedAt(LocalDateTime.now());

        receiverWallet.setUpdatedBy(requestDto.getCreatedBy());

        walletRepository.save(receiverWallet);

        log.info("Points added to receiver wallet");

        // SENDER TRANSACTION

        CoCustomerWalletTransactions senderTransaction = new CoCustomerWalletTransactions();

        senderTransaction.setWalletId(senderWallet.getWalletId());

        senderTransaction.setTransactionType(COConstants.POINTS_TRANSFERRED);

        senderTransaction.setPoints(-requestDto.getTransferPoints());

        senderTransaction.setCreatedAt(LocalDateTime.now());

        senderTransaction.setCreatedBy(requestDto.getCreatedBy());

        transactionsRepository.save(senderTransaction);

        log.info("Sender transaction saved");

        // RECEIVER TRANSACTION

        CoCustomerWalletTransactions receiverTransaction = new CoCustomerWalletTransactions();

        receiverTransaction.setWalletId(receiverWallet.getWalletId());

        receiverTransaction.setTransactionType(COConstants.POINTS_RECEIVED);

        receiverTransaction.setPoints(requestDto.getTransferPoints());

        receiverTransaction.setCreatedAt(LocalDateTime.now());

        receiverTransaction.setCreatedBy(requestDto.getCreatedBy());

        transactionsRepository.save(receiverTransaction);

        log.info("Receiver transaction saved");

        // RESPONSE

        CoWalletTransferResponseDto response = new CoWalletTransferResponseDto();

        response.setSuccess(true);

        response.setMessage(COConstants.POINTS_TRANSFER_SUCCESS);

        response.setSenderCustomerId(senderCustomer.getCustomerId());

        response.setReceiverCustomerId(receiverCustomer.getCustomerId());

        response.setTransferredPoints(requestDto.getTransferPoints());

        response.setSenderRemainingPoints(senderWallet.getBalancePoints());

        log.info("Wallet transfer completed successfully");

        return response;
    }

    @Override
    public CoCustomerResponseDto getCustomer(Integer customerId) {


        log.info("GET_CUSTOMER_SERVICE_START | customerId={}", customerId);

        log.info("GET_CUSTOMER_DB_FETCH_START | customerId={}", customerId);

        CoCustomer customer = customerRepository.findById(customerId).orElseThrow(() -> {

            log.error("GET_CUSTOMER_FAILED | customerId={} | reason=CUSTOMER_NOT_FOUND", customerId);

            return new CoBadRequestException(COConstants.MSG_CUSTOMER_NOT_FOUND);
        });

        log.info("GET_CUSTOMER_DB_FETCH_SUCCESS | customerId={} | email={}", customerId, customer.getEmail());

        CoCustomerResponseDto responseDto = customerMapper.mapToResponse(customer);

        log.info("GET_CUSTOMER_SERVICE_SUCCESS | customerId={} | executionTime={}ms", customerId);

        return responseDto;
    }

    @Override
    @Transactional
    public CoCustomerResponseDto updateCustomer(Integer customerId, CoCustomerRequestDto requestDto) {

        log.info("UPDATE_CUSTOMER_SERVICE_START | customerId={}", customerId);

        // VALIDATION LOGS

        log.info("UPDATE_CUSTOMER_VALIDATION_START | customerId={}", customerId);

        if (requestDto.getPhoneNumber() == null || requestDto.getPhoneNumber().isBlank()) {

            log.error("UPDATE_CUSTOMER_VALIDATION_FAILED | customerId={} | reason=PHONE_EMPTY", customerId);

            throw new CoBadRequestException("Phone number is required");
        }

        log.info("UPDATE_CUSTOMER_VALIDATION_SUCCESS | customerId={}", customerId);

        // DB FETCH LOGS

        log.info("UPDATE_CUSTOMER_DB_FETCH_START | customerId={}", customerId);

        CoCustomer customer = customerRepository.findById(customerId).orElseThrow(() -> {

            log.error("UPDATE_CUSTOMER_FAILED | customerId={} | reason=CUSTOMER_NOT_FOUND", customerId);

            return new CoBadRequestException(COConstants.MSG_CUSTOMER_NOT_FOUND);
        });

        log.info("UPDATE_CUSTOMER_DB_FETCH_SUCCESS | customerId={}", customerId);

        // CONTEXT LOGGING

        log.info("UPDATE_CUSTOMER_CONTEXT | customerId={} | existingEmail={} | newEmail={}", customerId, customer.getEmail(), requestDto.getEmail());

        customer.setFirstName(requestDto.getFirstName());
        customer.setLastName(requestDto.getLastName());
        customer.setEmail(requestDto.getEmail());
        customer.setPhoneNumber(requestDto.getPhoneNumber());

        // ==============================
        // GENERATE REFERRAL CODE
        // ==============================

        if (customer.getReferralCode() == null || customer.getReferralCode().isBlank()) {

            String referral = CoCustomerMapper.generateReferral(requestDto.getFirstName(), requestDto.getLastName(), requestDto.getPhoneNumber());

            customer.setReferralCode(referral);

            log.info("UPDATE_CUSTOMER_REFERRAL_GENERATED | customerId={} | referral={}", customerId, referral);
        }

        // ==============================
        // PROCESS REFERRAL CODE USED
        // ==============================

        if (requestDto.getReferralCodeUsed() != null && !requestDto.getReferralCodeUsed().isBlank()) {

            String referralCodeUsed = requestDto.getReferralCodeUsed().trim();

            log.info("REFERRAL_CODE_PROVIDED_ON_UPDATE | customerId={} | referralCode={}", customerId, referralCodeUsed);

            try {
                // --------------------------------
                // Find referrer using referral code
                // --------------------------------
                CoCustomer referrer = customerRepository.findByReferralCode(referralCodeUsed).orElseThrow(() -> new CoBadRequestException("Invalid referral code"));

                // --------------------------------
                // Customer cannot refer himself
                // --------------------------------

                if (referrer.getCustomerId().equals(customerId)) {
                    throw new CoBadRequestException("Customer cannot use own referral code");
                }

                // --------------------------------
                // Check existing referral
                // --------------------------------

                Optional<CoCustomerReferral> existingReferral = customerReferralRepository.findByRefereeCustomerId(customerId);

                if (existingReferral.isPresent()) {

                    log.warn("REFERRAL_ALREADY_EXISTS | customerId={} | referralId={}", customerId, existingReferral.get().getReferralId());

                } else {

                    // --------------------------------
                    // Create referral record
                    // --------------------------------

                    CoCustomerReferral referral = new CoCustomerReferral();

                    referral.setReferrerCustomerId(referrer.getCustomerId());
                    referral.setRefereeCustomerId(customerId);
                    referral.setReferralCode(referralCodeUsed);
                    referral.setReferralStatus(COConstants.REFERRAL_STATUS[0]);
                    referral.setReferralType("customer");
                    referral.setCreatedAt(LocalDateTime.now());
                    referral.setCreatedBy(requestDto.getCreatedBy());

                    customerReferralRepository.save(referral);

                    // --------------------------------
                    // Store referral code on customer
                    // --------------------------------

                    customer.setUsedReferral(referralCodeUsed);

                    log.info("REFERRAL_TRACKING_CREATED_ON_UPDATE | " + "referralId={} | referrerId={} | refereeCustomerId={} | referralCode={}", referral.getReferralId(), referrer.getCustomerId(), customerId, referralCodeUsed);
                }

            } catch (CoBadRequestException ex) {

                log.error("REFERRAL_UPDATE_FAILED | customerId={} | referralCode={} | error={}", customerId, referralCodeUsed, ex.getMessage());
                throw ex;
            } catch (Exception ex) {

                log.error("REFERRAL_UPDATE_FAILED | customerId={} | referralCode={} | error={}", customerId, referralCodeUsed, ex.getMessage(), ex);
                // Do not fail customer update for unexpected referral errors
            }
        }

        // ==============================\
        // UPDATE AUDIT INFORMATION
        // ==============================

        customer.setUpdatedAt(LocalDateTime.now());
        customer.setUpdatedBy(requestDto.getCreatedBy());

        // ==============================
        // SAVE CUSTOMER
        // ==============================

        try {

            customerRepository.save(customer);

            log.info("UPDATE_CUSTOMER_DB_SAVE_SUCCESS | customerId={}", customerId);

        } catch (DataAccessException ex) {

            log.error("UPDATE_CUSTOMER_DB_SAVE_FAILED | customerId={} | error={}", customerId, ex.getMessage(), ex);

            throw new CoBadRequestException(COConstants.MSG_DATABASE_ERROR);
        }

        CoCustomerResponseDto responseDto = customerMapper.mapToResponse(customer);

        log.info("UPDATE_CUSTOMER_SERVICE_SUCCESS | customerId={} | executionTime={}ms", customerId);

        return responseDto;
    }

    @Override
    public String updateCustomerProfile(CoCustomerRequestDto requestDto, MultipartFile profilePic) {
        log.info("UPDATE_PROFILE_STARTED | customerId={}", requestDto.getCustomerId());

        CoCustomer customer = customerRepository.findById(requestDto.getCustomerId()).orElseThrow(() -> {

            log.error("UPDATE_PROFILE_FAILED | customerId={} | reason=CUSTOMER_NOT_FOUND", requestDto.getCustomerId());

            return new CoBadRequestException(COConstants.MSG_CUSTOMER_NOT_FOUND);
        });

        customer.setFirstName(requestDto.getFirstName());
        customer.setLastName(requestDto.getLastName());
        customer.setEmail(requestDto.getEmail());
        customer.setDateOfBirth(requestDto.getDOB());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setUpdatedBy(requestDto.getCreatedBy());

        if (profilePic != null && !profilePic.isEmpty()) {
            try {
                String profilePicUrl = s3ImageService.uploadFile(profilePic, "customerProfilePic" + requestDto.getCustomerId());
                customer.setProfilePicUrl(profilePicUrl);
                log.info("UPDATE_PROFILE_PIC_UPLOAD_SUCCESS | customerId={} | url={}", requestDto.getCustomerId(), profilePicUrl);
            } catch (IOException ex) {
                log.error("UPDATE_PROFILE_PIC_UPLOAD_FAILED | customerId={} | error={}", requestDto.getCustomerId(), ex.getMessage(), ex);
                throw new CoBadRequestException("Failed to upload profile picture");
            }
        }

        try {
            customerRepository.save(customer);

            log.info("UPDATE_PROFILE_DB_SAVE_SUCCESS | customerId={}", requestDto.getCustomerId());
            return "Customer Profile Updated Successfully ";
        } catch (DataAccessException ex) {

            log.error("UPDATE_PROFILE_DB_SAVE_FAILED | customerId={} | error={}", requestDto.getCustomerId(), ex.getMessage(), ex);
            throw new CoBadRequestException(COConstants.MSG_DATABASE_ERROR);
        }
    }

    @Override
    public List<CoCustomerListDto> getAllCustomers() {

        log.info("GET_ALL_CUSTOMERS_SERVICE_START");

        List<CoCustomer> customers = customerRepository.findAll();

        log.info("GET_ALL_CUSTOMERS_DB_FETCH_SUCCESS | count={}", customers.size());

        List<FmAreaDto> areas = fmFeignClient.getAllAreas();

        log.info("GET_ALL_AREAS_FROM_FM_SUCCESS | count={}", areas.size());

        Map<Integer, String> areaMap = areas.stream().collect(Collectors.toMap(FmAreaDto::getAreaId, FmAreaDto::getAreaName));

        List<CoCustomerListDto> response = customers.stream().map(customer -> {

            log.debug("MAPPING_CUSTOMER | customerId={}", customer.getCustomerId());

            CoCustomerListDto dto = new CoCustomerListDto();

            dto.setCustomerId(customer.getCustomerId());

            dto.setCustomerName(customer.getFirstName() + " " + (customer.getLastName() == null ? "" : customer.getLastName()));

            dto.setEmail(customer.getEmail());

            dto.setPhoneNumber(customer.getPhoneNumber());

            dto.setAreaId(customer.getAreaId());

            dto.setAreaName(areaMap.getOrDefault(customer.getAreaId(), "-"));

            dto.setCreatedAt(customer.getCreatedAt());

            streakRepository.findTopByCustomerIdOrderByCheckInDateDesc(customer.getCustomerId()).ifPresent(streak -> {

                dto.setCurrentStreak(streak.getCurrentStreak());

                log.debug("CUSTOMER_STREAK_FOUND | customerId={} | streak={}", customer.getCustomerId(), streak.getCurrentStreak());
            });

            if (dto.getCurrentStreak() == null) {

                dto.setCurrentStreak(0);

                log.debug("CUSTOMER_STREAK_NOT_FOUND | customerId={}", customer.getCustomerId());
            }

            return dto;

        }).toList();

        log.info("GET_ALL_CUSTOMERS_SERVICE_SUCCESS | count={}", response.size());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CoCustomerWalletResponseDto getCustomerWallet(Integer customerId) {

        log.info("GET_CUSTOMER_WALLET_API_START | customerId={}", customerId);

        CoCustomerWallet wallet = walletRepository
                .findByCustomerCustomerId(customerId)
                .orElseThrow(() -> {
                    log.warn("GET_CUSTOMER_WALLET_API_FAILED | wallet not found | customerId={}", customerId);
                    return new CoBusinessException("Wallet is empty for customerId: " + customerId);
                });

        CoCustomer customer = wallet.getCustomer();

        String customerName = Stream.of(
                        customer.getFirstName(),
                        customer.getLastName()
                )
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.joining(" "));

        CoCustomerWalletResponseDto response = new CoCustomerWalletResponseDto();

        response.setWalletId(wallet.getWalletId());
        response.setCustomerId(customer.getCustomerId());
        response.setCustomerName(customerName);
        response.setReferralCode(customer.getReferralCode());
        response.setBalanceAmount(wallet.getBalanceAmount());
        response.setBalancePoints(wallet.getBalancePoints());

        log.info(
                "GET_CUSTOMER_WALLET_API_SUCCESS | customerId={} | walletId={}",
                customerId,
                wallet.getWalletId()
        );

        return response;
    }




    @Override
    public List<CoWalletTransactionHistoryDto> getWalletTransactionHistory(Integer customerId) {

        log.info("GET_WALLET_TRANSACTION_HISTORY_API_START | customerId={}", customerId);

        CoCustomerWallet wallet = walletRepository
                .findByCustomerCustomerId(customerId)
                .orElseThrow(() ->
                        new CoBusinessException("Wallet transactions not found for customerId: " + customerId));

        List<CoWalletTransactionHistoryDto> transactions =
                transactionsRepository
                        .findByWalletIdOrderByCreatedAtDesc(wallet.getWalletId())
                        .stream()
                        .map(transaction -> {

                            CoWalletTransactionHistoryDto dto =
                                    new CoWalletTransactionHistoryDto();

                            dto.setTransactionType(transaction.getTransactionType());
                            dto.setPoints(transaction.getPoints());
                            dto.setAmount(transaction.getAmount());
                            dto.setCreatedAt(transaction.getCreatedAt());

                            return dto;
                        })
                        .collect(Collectors.toList());

        log.info(
                "GET_WALLET_TRANSACTION_HISTORY_API_SUCCESS | customerId={}",
                customerId
        );

        return transactions;
    }

    @Override
    public List<CoProfileIncompleteCustomer> getProfileIncompleteCustomers() {

        log.info("Received request to fetch customers with incomplete profiles.");

        List<CoProfileIncompleteCustomer> customers = customerRepository.findAll().stream().filter(customer -> customer.getProfilePicUrl() == null || customer.getProfilePicUrl().isBlank()).map(customer -> {
            CoProfileIncompleteCustomer dto = new CoProfileIncompleteCustomer();
            dto.setCustomerId(customer.getCustomerId());
            dto.setFirstName(customer.getFirstName());
            dto.setLastName(customer.getLastName());
            dto.setEmail(customer.getEmail());
            dto.setPhoneNumber(customer.getPhoneNumber());
            return dto;
        }).collect(Collectors.toList());

        return customers;
    }

    // ================================================================
    // UPDATE CUSTOMER PROFILE PICTURE
    // ================================================================
    //
    // Only customerId and profilePicUrl are required.
    //
    // We directly find the customer by ID and update
    // profile_pic_url.
    //
    // We DO NOT access customerStatus here.
    // ================================================================

    @Override
    public String updateCustomerProfilePic(CustomerProfilePicDto customerDto) {

        log.info("[CUSTOMER] Updating profile picture. customerId={}", customerDto.getCustomerId());

        // ============================================================
        // 1. Validate customer ID
        // ============================================================

        if (customerDto.getCustomerId() == null) {

            throw new IllegalArgumentException("Customer ID is required");
        }

        // ============================================================
        // 2. Validate profile picture URL
        // ============================================================

        if (customerDto.getProfilePicUrl() == null || customerDto.getProfilePicUrl().trim().isEmpty()) {

            throw new IllegalArgumentException("Profile picture URL is required");
        }

        // ============================================================
        // 3. Find customer directly by ID
        // ============================================================

        CoCustomer customer = customerRepository.findById(customerDto.getCustomerId()).orElseThrow(() -> new CoResourceNotFoundException("Customer not found with id: " + customerDto.getCustomerId()));

        // ============================================================
        // 4. Update ONLY profile picture URL
        // ============================================================

        customer.setProfilePicUrl(customerDto.getProfilePicUrl());

        // ============================================================
        // 5. Save customer
        // ============================================================

        customerRepository.save(customer);

        log.info("[CUSTOMER] Profile picture updated successfully. " + "customerId={}", customerDto.getCustomerId());

        // ============================================================
        // 6. Return success message with profile picture URL
        // ============================================================

        return "Customer profile picture updated successfully. " + "Profile picture url: " + customerDto.getProfilePicUrl();
    }
//    ===============================================================================
//    ===============================================================================

    /**
     * Fetches complete order flow counts based on order status.
     * <p>
     * This method retrieves all five required counts:
     * <p>
     * 1. Total orders
     * 2. Orders placed
     * 3. Orders confirmed
     * 4. Orders shipped
     * 5. Orders completed
     * 6. Orders Rejected
     *
     * @return complete order flow count response
     */
    @Override
    public CoCompleteOrdersFlowCountsDto getCompleteOrdersFlowCounts() {

        log.info("Fetching complete orders flow counts");

        CoCompleteOrdersFlowCountsProjection projection = customerRepository.getCompleteOrdersFlowCounts();

        if (projection == null) {

            log.warn("No order flow count data found");

            return new CoCompleteOrdersFlowCountsDto();
        }

        CoCompleteOrdersFlowCountsDto response = CoCustomerMapper.mapToCompleteOrdersFlowCountsDto(projection);

        log.info("Complete orders flow counts fetched successfully. " + "Total: {}, Placed: {}, Confirmed: {}, Shipped: {}, Completed: {}", response.getTotalOrdersCount(), response.getOrdersPlaced(), response.getOrdersConfirmed(), response.getOrdersShipped(), response.getOrdersCompleted());

        return response;
    }
//    =================================================================================
//    =================================================================================
    /**
     * Fetches complete order details based on order status.
     *
     * The requested status is passed from the controller
     * and used to filter records from the orders table.
     *
     * @param orderStatus order status filter
     * @return list of orders matching the requested status
     */
    /**
     * Fetches complete order details based on order status.
     * <p>
     * CO database provides:
     * - Order details
     * - Customer name
     * - Order amount
     * <p>
     * FM microservice provides:
     * - Outlet name
     * - Area name
     * <p>
     * Driver microservice provides:
     * - Driver name
     * <p>
     * Bulk APIs are used to avoid making one network request
     * for every individual order.
     */
    @Override
    public Page<CoOrderDetailsByOrderStatusDto> getCompleteOrdersDetailsByOrderStatus(
            String orderStatus,
            Pageable pageable) {

        log.info(
                "Fetching complete order details. orderStatus={}, page={}, size={}",
                orderStatus,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        // --------------------------------------------------------
        // Step 1: Fetch paginated order information from CO database
        // --------------------------------------------------------

        Page<CoOrderDetailsByOrderStatusProjection> projectionPage =
                customerRepository.getCompleteOrdersDetailsByOrderStatus(
                        orderStatus,
                        pageable
                );

        log.info(
                "Found {} orders in current page. Total orders={}, orderStatus={}",
                projectionPage.getNumberOfElements(),
                projectionPage.getTotalElements(),
                orderStatus
        );

        // --------------------------------------------------------
        // If no orders found
        // --------------------------------------------------------

        if (projectionPage.isEmpty()) {

            log.info(
                    "No orders found for orderStatus={}, page={}",
                    orderStatus,
                    pageable.getPageNumber()
            );

            return Page.empty(pageable);
        }

        // --------------------------------------------------------
        // Step 2: Prepare outlet IDs and driver IDs
        // --------------------------------------------------------

        List<Integer> outletIds = new ArrayList<>();

        List<Integer> driverIds = new ArrayList<>();

        for (CoOrderDetailsByOrderStatusProjection projection :
                projectionPage.getContent()) {

            // Add outlet ID if it is not already present
            addIfNotPresent(
                    outletIds,
                    projection.getOutletId()
            );

            // Add driver ID if it is not already present
            addIfNotPresent(
                    driverIds,
                    projection.getDriverId()
            );
        }

        log.info(
                "Preparing FM request for {} outlet IDs",
                outletIds.size()
        );

        log.info(
                "Preparing Driver request for {} driver IDs",
                driverIds.size()
        );

        // --------------------------------------------------------
        // Step 3: Fetch outlet information from FM service
        // --------------------------------------------------------

        List<CoFmOutletDetailsDto> outletDetails =
                new ArrayList<>();

        if (!outletIds.isEmpty()) {

            CoOutletDetailsRequestDto outletRequest =
                    new CoOutletDetailsRequestDto();

            outletRequest.setOutletIds(outletIds);

            outletDetails =
                    fmFeignClient.getOutletDetailsByIds(
                            outletRequest
                    );

            if (outletDetails != null) {

                log.info(
                        "Received {} outlet details from FM service",
                        outletDetails.size()
                );

            } else {

                log.warn(
                        "FM service returned null outlet details"
                );
            }
        }

        // --------------------------------------------------------
        // Step 4: Fetch driver information from Driver service
        // --------------------------------------------------------

        List<CoDriverDetailsDto> driverDetails =
                new ArrayList<>();

        if (!driverIds.isEmpty()) {

            CoDriverDetailsRequestDto driverRequest =
                    new CoDriverDetailsRequestDto();

            driverRequest.setDriverIds(driverIds);

            driverDetails =
                    driverFeignClient.getDriverDetailsByIds(
                            driverRequest
                    );

            if (driverDetails != null) {

                log.info(
                        "Received {} driver details from Driver service",
                        driverDetails.size()
                );

            } else {

                log.warn(
                        "Driver service returned null driver details"
                );
            }
        }

        // --------------------------------------------------------
        // Step 5: Map current page data into final response
        // --------------------------------------------------------

        List<CoOrderDetailsByOrderStatusDto> responseList =
                CoCustomerMapper.mapToCompleteOrderDetails(
                        projectionPage.getContent(),
                        outletDetails,
                        driverDetails
                );

        log.info(
                "Successfully mapped {} orders for current page",
                responseList.size()
        );

        // --------------------------------------------------------
        // Step 6: Create paginated response
        // --------------------------------------------------------

        Page<CoOrderDetailsByOrderStatusDto> responsePage =
                new PageImpl<>(
                        responseList,
                        pageable,
                        projectionPage.getTotalElements()
                );

        log.info(
                "Successfully prepared paginated response. " +
                        "page={}, size={}, currentElements={}, totalElements={}, totalPages={}",
                responsePage.getNumber(),
                responsePage.getSize(),
                responsePage.getNumberOfElements(),
                responsePage.getTotalElements(),
                responsePage.getTotalPages()
        );

        return responsePage;
    }
//    ==============================================================================
//    ==============================================================================

    /**
     * Adds an ID to the list only when it is not already present.
     */
    private void addIfNotPresent(List<Integer> ids, Integer id) {

        if (id == null) {
            return;
        }

        if (!ids.contains(id)) {
            ids.add(id);
        }
    }

    //    =================================================================================
//    =================================================================================
    @Override
    public CoOrderCompleteDetailsResponseDto getOrderCompleteDetails(String orderId) {

        log.info("Fetching complete order details. orderId={}", orderId);

        /*
         * STEP 1
         * Fetch main order information from CO database.
         *
         * This also fetches:
         * - customer
         * - customer address
         * - payment mode
         * - outlet ID
         */
        CoOrderCompleteDetailsProjection orderProjection
                = coOrderRepository.getOrderCompleteDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        log.info("Main order details fetched successfully. " + "orderId={}, customerId={}, outletId={}, driverId={}, status={}", orderId, orderProjection.getCustomerId(), orderProjection.getOutletId(), orderProjection.getDriverId(), orderProjection.getOrderStatus());

        CoOrderCompleteDetailsResponseDto response
                = CoOrderCompleteDetailsMapper.mapMainDetails(orderProjection);

        log.info("Main order details fetched successfully. " + "orderId={}, customerId={}, outletId={}, status={}", orderId, orderProjection.getCustomerId(), orderProjection.getOutletId(), orderProjection.getOrderStatus());

        /*
         * STEP 2
         * Fetch outlet information from Food & Mart microservice.
         */
        try {

            if (orderProjection.getOutletId() != null) {

                log.info("Calling FM service for outlet details. " + "outletId={}", orderProjection.getOutletId());

                CoOutletDetailsDto outletDetails = fmFeignClient.getOutletCompleteDetails(orderProjection.getOutletId());

                response.setOutlet(outletDetails);

                log.info("FM outlet details fetched successfully. " + "outletId={}", orderProjection.getOutletId());
            }

        } catch (Exception e) {

            log.error("Failed to fetch outlet details from FM service. " + "outletId={}", orderProjection.getOutletId(), e);

            throw new RuntimeException("Unable to fetch outlet details");
        }
        /*
         * STEP 3
         * Fetch driver information from Driver microservice.
         *
         * One order can have only one assigned driver.
         * The driver ID is stored in the orders table.
         *
         * Driver name and mobile number are fetched
         * from Driver microservice using Feign.
         */
        try {

            if (orderProjection.getDriverId() != null) {

                log.info("Calling Driver service for driver details. " + "driverId={}, orderId={}", orderProjection.getDriverId(), orderId);

                CoDriverDetailsDto driverDetails = driverFeignClient.getDriverDetailsForOrder(orderProjection.getDriverId());

                response.setDriver(driverDetails);

                log.info("Driver details fetched successfully. " + "driverId={}, orderId={}", orderProjection.getDriverId(), orderId);

            } else {

                log.info("No driver assigned to order. orderId={}", orderId);

                response.setDriver(null);
            }

        } catch (Exception e) {

            log.error("Failed to fetch driver details from Driver service. " + "driverId={}, orderId={}", orderProjection.getDriverId(), orderId, e);

            /*
             * Driver details should not prevent
             * the complete order response from being returned.
             */
            response.setDriver(null);
        }

        /*
         * STEP 3.1
         * Fetch order items from CO database.
         */
        log.info("Fetching order items. orderId={}", orderId);

        List<CoOrderItemProjection> itemProjections = coOrderItemRepository.getOrderItems(orderId);

        response.setItems(CoOrderCompleteDetailsMapper.mapOrderItems(itemProjections));

        log.info("Order items fetched successfully. " + "orderId={}, itemCount={}", orderId, itemProjections.size());

        /*
         * STEP 4
         * Fetch price breakup from CO database.
         */
        log.info("Fetching order price breakup. orderId={}", orderId);

        CoOrderPriceBreakupProjection priceProjection =
                coOrderPriceBreakupRepository.getPriceBreakup(orderId).orElse(null);

        if (priceProjection != null) {

            response.setPriceBreakup(CoOrderCompleteDetailsMapper.mapPriceBreakup(priceProjection));

            log.info("Order price breakup fetched successfully. " + "orderId={}", orderId);

        } else {

            log.warn("Price breakup not found for orderId={}", orderId);
        }

        /*
         * STEP 5
         * Refund details are required ONLY when
         * order status is ORDER_REJECTED.
         */
        if (COConstants.ORDER_STATUS_REJECTED.equalsIgnoreCase(orderProjection.getOrderStatus())) {

            log.info("Order is rejected. Fetching refund details " + "from Division service. orderId={}", orderId);

            try {

                CoRefundDetailsDto refundDetails = divisionFeignClient.getRefundDetails(orderId);

                log.info("Division refund API response received. orderId={}, refundDetails={}", orderId, refundDetails);

                log.info("Refund response received from Division. orderId={}, refundDetails={}", orderId, refundDetails);

                response.setRefund(refundDetails);

                log.info("Refund details set in complete order response. orderId={}", orderId);

            } catch (Exception e) {

                log.error("Failed to fetch refund details from " + "Division service. orderId={}", orderId, e);

                /*
                 * We can keep refund as null if refund record
                 * does not exist yet.
                 */
                response.setRefund(null);
            }

        } else {

            log.info("Order is not rejected. Refund details are not required. " + "orderId={}, status={}", orderId, orderProjection.getOrderStatus());

            response.setRefund(null);
        }

        log.info("Complete order details fetched successfully. " + "orderId={}", orderId);

        return response;
    }

    //=======================================================================================
    @Override
    public CoOrderFlowCountForMerchantOutletOrDriverDto getOrderFlowCountForMerchantOrOutletOrDriver(Integer merchantId, Integer outletId, Integer driverId) {

        log.info("Fetching order flow counts. merchantId={}, outletId={}, driverId={}", merchantId, outletId, driverId);

        // ============================================================
        // STEP 1: Validate input
        // ============================================================
        //
        // Exactly ONE of the following must be provided:
        //
        // merchantId
        // OR
        // outletId
        // OR
        // driverId
        //
        // ============================================================

        int providedParameters = 0;

        if (merchantId != null) {
            providedParameters++;
        }

        if (outletId != null) {
            providedParameters++;
        }

        if (driverId != null) {
            providedParameters++;
        }

        // ============================================================
        // No identifier provided
        // ============================================================

        if (providedParameters == 0) {

            log.warn("Neither merchantId, outletId nor driverId was provided");

            throw new IllegalArgumentException("Either merchantId, outletId or driverId must be provided");
        }

        // ============================================================
        // Multiple identifiers provided
        // ============================================================

        if (providedParameters > 1) {

            log.warn("Multiple identifiers provided. " + "merchantId={}, outletId={}, driverId={}", merchantId, outletId, driverId);

            throw new IllegalArgumentException("Only one of merchantId, outletId or driverId can be provided");
        }


        // ============================================================
        // STEP 2: Prepare outlet IDs
        // ============================================================

        List<Integer> outletIds = new ArrayList<>();


        // ============================================================
        // CASE 1: Merchant ID provided
        // ============================================================
        //
        // CO → Feign → FM
        //
        // FM returns all outlet IDs belonging to merchant.
        //
        // CO then calculates order counts for those outlets.
        // ============================================================

        if (merchantId != null) {

            log.info("Merchant ID provided. Fetching outlets from FM. " + "merchantId={}", merchantId);

            List<Integer> merchantOutletIds = fmFeignClient.getOutletIdsByMerchantId(merchantId);

            if (merchantOutletIds == null || merchantOutletIds.isEmpty()) {

                log.info("No outlets found for merchant. merchantId={}", merchantId);

                return createEmptyOrderFlowCountForMerchantOrOutletOrDriverResponse();
            }

            // Add merchant outlet IDs
            for (Integer merchantOutletId : merchantOutletIds) {

                if (merchantOutletId != null) {

                    outletIds.add(merchantOutletId);
                }
            }

            log.info("Found {} outlets for merchant. merchantId={}", outletIds.size(), merchantId);

            // ========================================================
            // Fetch order counts for merchant outlets
            // ========================================================

            CoOrderFlowCountProjection projection = coOrderRepository.getOrderFlowCountsByOutletIds(outletIds);

            return CoCustomerMapper.mapToOrderFlowCountForMerchantOrOutletOrDriver(projection);
        }


        // ============================================================
        // CASE 2: Outlet ID provided
        // ============================================================
        //
        // No FM call.
        //
        // Directly query CO orders table using outlet_id.
        // ============================================================

        if (outletId != null) {

            log.info("Outlet ID provided. Fetching order counts. " + "outletId={}", outletId);

            outletIds.add(outletId);

            CoOrderFlowCountProjection projection = coOrderRepository.getOrderFlowCountsByOutletIds(outletIds);

            return CoCustomerMapper.mapToOrderFlowCountForMerchantOrOutletOrDriver(projection);
        }


        // ============================================================
        // CASE 3: Driver ID provided
        // ============================================================
        //
        // No FM call.
        //
        // Directly query CO orders table using driver_id.
        // ============================================================

        log.info("Driver ID provided. Fetching order counts. " + "driverId={}", driverId);

        CoOrderFlowCountProjection projection = coOrderRepository.getOrderFlowCountsByDriverId(driverId);


        // ============================================================
        // STEP 3: Map projection to response DTO
        // ============================================================

        CoOrderFlowCountForMerchantOutletOrDriverDto response = CoCustomerMapper.mapToOrderFlowCountForMerchantOrOutletOrDriver(projection);

        log.info("Driver order flow counts fetched successfully. " + "driverId={}, total={}, completed={}, rejected={}", driverId, response.getTotalOrdersCount(), response.getCompletedOrdersCount(), response.getRejectedOrdersCount());

        return response;
    }

//===============================================================================
//========================= HELPER METHODS ======================================
//===============================================================================

    /**
     * Creates an empty order flow count response.
     * <p>
     * This response is returned when:
     * - Merchant has no outlets
     * - No orders are found for the given driver
     * - No orders are found for the given outlet
     */
    private CoOrderFlowCountForMerchantOutletOrDriverDto createEmptyOrderFlowCountForMerchantOrOutletOrDriverResponse() {

        CoOrderFlowCountForMerchantOutletOrDriverDto dto = new CoOrderFlowCountForMerchantOutletOrDriverDto();

        dto.setTotalOrdersCount(0L);
        dto.setCompletedOrdersCount(0L);
        dto.setRejectedOrdersCount(0L);

        return dto;
    }

    //    ============================================================================
//    ============================================================================
    @Override
    public Page<CoOrderDetailsOfOutletDto> getOrderDetailsOfOutlet(Integer outletId, Pageable pageable) {

        log.info("Fetching order details for outletId={}, page={}, size={}", outletId, pageable.getPageNumber(), pageable.getPageSize());

        // Fetch paginated order details from CO database
        Page<CoOrderDetailsOfOutletProjection> projectionPage = coOrderRepository.getOrderDetailsOfOutlet(outletId, pageable);

        if (projectionPage == null || projectionPage.isEmpty()) {

            log.info("No orders found for outletId={}", outletId);

            return Page.empty(pageable);
        }

        // ============================================================
        // OUTLET DETAILS FROM FM MICROSERVICE
        // ============================================================

        // FM API expects a List of outlet IDs.
        // Since this API is for one outlet:
        // outletId = 13 → outletIds = [13]

        CoOutletDetailsRequestDto request = new CoOutletDetailsRequestDto();

        request.setOutletIds(Collections.singletonList(outletId));

        // Fetch outlet name and area name from FM
        List<CoFmOutletDetailsDto> outletDetails = fmFeignClient.getOutletDetailsByIds(request);

        CoFmOutletDetailsDto outlet = null;

        if (outletDetails != null && !outletDetails.isEmpty()) {
            outlet = outletDetails.get(0);
        }

        // ============================================================
        // CREATE RESPONSE LIST
        // ============================================================

        List<CoOrderDetailsOfOutletDto> responseList = new ArrayList<>();

        // ============================================================
        // PROCESS EACH ORDER
        // ============================================================

        for (CoOrderDetailsOfOutletProjection projection : projectionPage.getContent()) {

            log.info("Processing orderId={}", projection.getOrderId());

            // Map CO database details to DTO
            CoOrderDetailsOfOutletDto dto = CoCustomerMapper.mapToOrderDetailsOfOutlet(projection);

            // ========================================================
            // OUTLET DETAILS
            // ========================================================

            if (outlet != null) {

                dto.setOutletName(outlet.getOutletName());

                dto.setAreaName(outlet.getAreaName());
            }

            // ========================================================
            // DRIVER DETAILS
            // ========================================================

            if (projection.getDriverId() != null) {

                log.info("Fetching driver details for driverId={}", projection.getDriverId());

                CoDriverDetailsDto driver = driverFeignClient.getDriverDetailsForOrder(projection.getDriverId());

                if (driver != null) {

                    dto.setDriverName(driver.getDriverName());

                    dto.setDriverMobileNumber(driver.getDriverMobileNumber());
                }
            }

            responseList.add(dto);
        }

        log.info("Successfully fetched {} orders for outletId={}", responseList.size(), outletId);

        // Return paginated response
        return new PageImpl<>(responseList, pageable, projectionPage.getTotalElements());
    }

    //  ====================================================================================
//  ====================================================================================
    @Override
    public Page<CoOrderDetailsOfDriverDto> getOrderDetailsOfDriver(
            Integer driverId,
            Pageable pageable) {

        log.info(
                "Fetching order details for driverId={}, page={}, size={}",
                driverId,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        // ============================================================
        // FETCH ORDER DETAILS FROM CO DATABASE
        // ============================================================

        Page<CoOrderDetailsOfDriverProjection> projectionPage =
                coOrderRepository.getOrderDetailsOfDriver(
                        driverId,
                        pageable
                );

        if (projectionPage == null || projectionPage.isEmpty()) {

            log.info(
                    "No orders found for driverId={}",
                    driverId
            );

            return Page.empty(pageable);
        }

        List<CoOrderDetailsOfDriverDto> responseList =
                new ArrayList<>();

        // ============================================================
        // PROCESS EACH ORDER
        // ============================================================

        for (CoOrderDetailsOfDriverProjection projection :
                projectionPage.getContent()) {

            log.info(
                    "Processing orderId={}, driverId={}",
                    projection.getOrderId(),
                    driverId
            );

            // ========================================================
            // MAP CO DATABASE DATA TO DTO
            // ========================================================

            CoOrderDetailsOfDriverDto dto =
                    CoCustomerMapper.mapToOrderDetailsOfDriver(
                            projection
                    );

            // ========================================================
            // OUTLET DETAILS FROM FM MICROSERVICE
            // ========================================================

            if (projection.getOutletId() != null) {

                log.info(
                        "Fetching outlet details for outletId={}",
                        projection.getOutletId()
                );

                try {

                    CoOutletDetailsDto outlet =
                            fmFeignClient.getOutletCompleteDetails(
                                    projection.getOutletId()
                            );

                    if (outlet != null) {

                        dto.setOutletName(
                                outlet.getOutletName()
                        );

                    } else {

                        log.warn(
                                "Outlet details returned null for outletId={}",
                                projection.getOutletId()
                        );
                    }

                } catch (Exception e) {

                    // If outlet is not available in FM,
                    // don't fail the complete driver orders API.
                    // outletName will remain null.

                    log.warn(
                            "Unable to fetch outlet details from FM for outletId={}. " +
                                    "Continuing with null outletName.",
                            projection.getOutletId(),
                            e
                    );
                }
            }

            // ========================================================
            // DRIVER DETAILS FROM DRIVER MICROSERVICE
            // ========================================================

            if (projection.getDriverId() != null) {

                log.info(
                        "Fetching driver details for driverId={}",
                        projection.getDriverId()
                );

                CoDriverDetailsDto driver =
                        driverFeignClient.getDriverDetailsForOrder(
                                projection.getDriverId()
                        );

                if (driver != null) {

                    dto.setDriverName(
                            driver.getDriverName()
                    );

                    dto.setDriverMobileNumber(
                            driver.getDriverMobileNumber()
                    );
                }
            }

            // Add completed DTO to response list
            responseList.add(dto);
        }

        // ============================================================
        // CREATE PAGINATED RESPONSE
        // ============================================================

        Page<CoOrderDetailsOfDriverDto> responsePage =
                new PageImpl<>(
                        responseList,
                        pageable,
                        projectionPage.getTotalElements()
                );

        log.info(
                "Successfully fetched {} orders for driverId={}, totalElements={}",
                responseList.size(),
                driverId,
                projectionPage.getTotalElements()
        );

        return responsePage;
    }
}
