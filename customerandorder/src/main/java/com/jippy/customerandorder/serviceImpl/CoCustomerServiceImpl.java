package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.*;
import com.jippy.customerandorder.exception.CoBusinessException;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.ICoCustomerService;
import com.jippy.customerandorder.mapper.CoCustomerMapper;
import com.jippy.customerandorder.repository.*;
import com.jippy.customerandorder.entity.CoCustomerReferral;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jippy.customerandorder.exception.CoBadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoCustomerServiceImpl implements ICoCustomerService {

    private final CoCustomerRepository customerRepository;

    private final CoCustomerWalletRepository walletRepository;

    private final CoWalletSettingsRepository walletSettingsRepository;

    private final CoCustomerWalletTransactionsRepository transactionsRepository;

    private final CoCustomerStreakRepository streakRepository;

    private final CoCustomerMapper customerMapper;

    private final CoCustomerReferralRepository customerReferralRepository;

    private final FMFeignClient fmFeignClient;

    private final S3ImageService s3ImageService;


    // QUALIFY REFERRAL ON FIRST ORDER
    @Override
    @Transactional
    public void qualifyReferralOnFirstOrder(Integer customerId) {
        log.info("REFERRAL_QUALIFICATION_START | customerId={}", customerId);

        // Check if customer has a pending referral
        Optional<CoCustomerReferral> referralOpt = customerReferralRepository
                .findByRefereeCustomerIdAndReferralStatus(customerId, COConstants.REFERRAL_STATUS[0]);

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

            log.info("REFERRAL_QUALIFIED_SUCCESS | referralId={} | referrerId={} | refereeCustomerId={}",
                    referral.getReferralId(), referral.getReferrerCustomerId(), customerId);

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
        Optional<CoCustomerReferral> referralOpt = customerReferralRepository
                .findByRefereeCustomerIdAndReferralStatus(customerId, COConstants.REFERRAL_STATUS[1]);

        if (referralOpt.isEmpty()) {
            log.info("REFERRAL_REWARD_NO_QUALIFIED | customerId={}", customerId);
            return;
        }

        CoCustomerReferral referral = referralOpt.get();

        try {
            // Get referrer's wallet
            CoCustomerWallet referrerWallet = walletRepository
                    .findByCustomerCustomerId(referral.getReferrerCustomerId())
                    .orElseThrow(() -> new CoBusinessException(COConstants.WALLET_NOT_FOUND));

            // Add 250 points to referrer's wallet
            Integer currentBalance = referrerWallet.getBalancePoints() != null ? referrerWallet.getBalancePoints() : 0;
            referrerWallet.setBalancePoints(currentBalance + COConstants.REFERRAL_REWARD_POINTS);
            referrerWallet.setUpdatedAt(LocalDateTime.now());
            referrerWallet.setUpdatedBy(1); // System user
            walletRepository.save(referrerWallet);

            log.info("REFERRAL_REWARD_WALLET_UPDATED | referrerId={} | pointsAdded={} | newBalance={}",
                    referral.getReferrerCustomerId(), COConstants.REFERRAL_REWARD_POINTS, referrerWallet.getBalancePoints());

            // Record transaction
            CoCustomerWalletTransactions transaction = new CoCustomerWalletTransactions();
            transaction.setWalletId(referrerWallet.getWalletId());
            transaction.setTransactionType(COConstants.REFERRAL_REWARD);
            transaction.setPoints(COConstants.REFERRAL_REWARD_POINTS);
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setCreatedBy(1);
            transactionsRepository.save(transaction);


            // Update referral record status to rewarded
            referral.setReferralStatus(COConstants.REFERRAL_STATUS[2]);
            referral.setUpdatedAt(LocalDateTime.now());
            referral.setUpdatedBy(1);
            customerReferralRepository.save(referral);

            log.info("REFERRAL_REWARD_PROCESSED_SUCCESS | referralId={} | referrerId={} | refereeCustomerId={}",
                    referral.getReferralId(), referral.getReferrerCustomerId(), customerId);

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

        CoCustomer customer = customerRepository
                .findById(dto.getCustomerId())
                .orElseThrow(() -> {

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

                log.warn(
                        "PHONE_NUMBER_MISMATCH | customerId={} | " +
                                "verifiedPhone={} | requestPhone={}",
                        customer.getCustomerId(),
                        verifiedPhone,
                        requestPhone
                );
                throw new CoBadRequestException("Phone number mismatch");
            }

            log.info(
                    "PHONE_NUMBER_VERIFIED | customerId={} | phone={}",
                    customer.getCustomerId(),
                    requestPhone
            );
        }

        // ==========================================
        // CHECK EMAIL
        // ==========================================

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {

            Optional<CoCustomer> existingEmail = customerRepository.findByEmail(
                            dto.getEmail().trim());

            if (existingEmail.isPresent() &&
                    !existingEmail.get().getCustomerId()
                            .equals(customer.getCustomerId())) {

                log.error(
                        "EMAIL_ALREADY_EXISTS | email={} | existingCustomerId={}",
                        dto.getEmail(),
                        existingEmail.get().getCustomerId()
                );
                throw new CoBadRequestException(
                        COConstants.EMAIL_ALREADY_EXISTS);
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
        if (customer.getReferralCode() == null ||
                customer.getReferralCode().isBlank()) {

            String referralCode =
                    CoCustomerMapper.generateReferral(
                            dto.getFirstName(),
                            dto.getLastName(),
                            dto.getPhoneNumber()
                    );

            customer.setReferralCode(referralCode);

            log.info("REFERRAL_CODE_GENERATED | customerId={} | referralCode={}",
                    customer.getCustomerId(), referralCode);
        }

        customer.setUpdatedAt(LocalDateTime.now());
        customer.setUpdatedBy(dto.getCreatedBy());

        CoCustomer savedCustomer = customerRepository.save(customer);

        log.info(
                "CUSTOMER_UPDATED | customerId={}",
                savedCustomer.getCustomerId()
        );

        // ==========================================
        // PROCESS REFERRAL
        // ==========================================

        if (dto.getReferralCodeUsed() != null && !dto.getReferralCodeUsed().isBlank()) {
            String referralCode = dto.getReferralCodeUsed().trim();

            try {
                CoCustomer referrer =
                        customerRepository
                                .findByReferralCode(referralCode)
                                .orElseThrow(() ->
                                        new CoBadRequestException(
                                                "Invalid referral code")
                                );
                // Prevent self referral
                if (referrer.getCustomerId()
                        .equals(savedCustomer.getCustomerId())) {

                    throw new CoBadRequestException(
                            "Customer cannot use own referral code"
                    );
                }

                Optional<CoCustomerReferral> existingReferral =
                        customerReferralRepository
                                .findByRefereeCustomerId(
                                        savedCustomer.getCustomerId()
                                );

                if (existingReferral.isPresent()) {

                    log.info(
                            "REFERRAL_ALREADY_EXISTS | customerId={} | referralId={}",
                            savedCustomer.getCustomerId(),
                            existingReferral.get().getReferralId()
                    );

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
                    log.info(
                            "REFERRAL_CREATED | referralId={} | " +
                                    "referrerId={} | refereeId={}",
                            referral.getReferralId(),
                            referrer.getCustomerId(),
                            savedCustomer.getCustomerId()
                    );
                }
            } catch (CoBadRequestException ex) {
                log.error(
                        "REFERRAL_FAILED | customerId={} | " +
                                "referralCode={} | error={}",
                        savedCustomer.getCustomerId(),
                        referralCode,
                        ex.getMessage()
                );
                throw ex;
            }
        }

        // ==========================================
        // CREATE WALLET IF NOT EXISTS
        // ==========================================

        Optional<CoCustomerWallet> existingWallet =
                walletRepository.findByCustomerCustomerId(savedCustomer.getCustomerId());

        if (existingWallet.isEmpty()) {
            log.info("WALLET_NOT_FOUND | customerId={} | creating wallet",
                    savedCustomer.getCustomerId()
            );

            CoWalletSettings walletSettings =
                    walletSettingsRepository
                            .findBySettingType(
                                    COConstants.WELCOME_POINTS
                            )
                            .orElseThrow(() ->
                                    new CoBusinessException(
                                            COConstants.WELCOME_POINTS_NOT_CONFIGURED
                                    )
                            );

            CoCustomerWallet wallet =
                    CoCustomerMapper.mapToWallet(
                            savedCustomer,
                            walletSettings.getSettingValue(),
                            dto.getCreatedBy()
                    );

            CoCustomerWallet savedWallet = walletRepository.save(wallet);

            log.info(
                    "WALLET_CREATED | customerId={} | walletId={} | points={}",
                    savedCustomer.getCustomerId(),
                    savedWallet.getWalletId(),
                    savedWallet.getBalancePoints()
            );

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
            log.info(
                    "WELCOME_TRANSACTION_CREATED | " +
                            "customerId={} | walletId={} | points={}",
                    savedCustomer.getCustomerId(),
                    savedWallet.getWalletId(),
                    walletSettings.getSettingValue()
            );

        } else {

            log.info(
                    "WALLET_ALREADY_EXISTS | customerId={} | walletId={}",
                    savedCustomer.getCustomerId(),
                    existingWallet.get().getWalletId()
            );
        }

        log.info(
                "CUSTOMER_REGISTRATION_COMPLETION_SUCCESS | customerId={}",
                savedCustomer.getCustomerId()
        );

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
        transactionsRepository.save(transaction);
        log.info("Points conversion transaction saved");

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

        // DAILY POINTS

        Integer streakPoints = streakSettings.getSettingValue();

        // MINIMUM STREAK DAYS

        Integer streakDays = streakSettings.getSettingValue();

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

            String referral = CoCustomerMapper.generateReferral(
                    requestDto.getFirstName(),
                    requestDto.getLastName(),
                    requestDto.getPhoneNumber()
            );

            customer.setReferralCode(referral);

            log.info(
                    "UPDATE_CUSTOMER_REFERRAL_GENERATED | customerId={} | referral={}",
                    customerId,
                    referral
            );
        }

        // ==============================
        // PROCESS REFERRAL CODE USED
        // ==============================

        if (requestDto.getReferralCodeUsed() != null && !requestDto.getReferralCodeUsed().isBlank()) {

            String referralCodeUsed = requestDto.getReferralCodeUsed().trim();

            log.info("REFERRAL_CODE_PROVIDED_ON_UPDATE | customerId={} | referralCode={}",
                    customerId,
                    referralCodeUsed);

            try {
                // --------------------------------
                // Find referrer using referral code
                // --------------------------------
                CoCustomer referrer = customerRepository
                        .findByReferralCode(referralCodeUsed)
                        .orElseThrow(() ->
                                new CoBadRequestException(
                                        "Invalid referral code"
                                )
                        );

                // --------------------------------
                // Customer cannot refer himself
                // --------------------------------

                if (referrer.getCustomerId().equals(customerId)) {
                    throw new CoBadRequestException("Customer cannot use own referral code");}

                // --------------------------------
                // Check existing referral
                // --------------------------------

                Optional<CoCustomerReferral> existingReferral =
                        customerReferralRepository
                                .findByRefereeCustomerId(customerId);

                if (existingReferral.isPresent()) {

                    log.warn(
                            "REFERRAL_ALREADY_EXISTS | customerId={} | referralId={}",
                            customerId,
                            existingReferral.get().getReferralId()
                    );

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

                    log.info(
                            "REFERRAL_TRACKING_CREATED_ON_UPDATE | " +
                                    "referralId={} | referrerId={} | refereeCustomerId={} | referralCode={}",
                            referral.getReferralId(),
                            referrer.getCustomerId(),
                            customerId,
                            referralCodeUsed
                    );
                }

            } catch (CoBadRequestException ex) {

                log.error(
                        "REFERRAL_UPDATE_FAILED | customerId={} | referralCode={} | error={}",
                        customerId,
                        referralCodeUsed,
                        ex.getMessage()
                );
                throw ex;
            } catch (Exception ex) {

                log.error(
                        "REFERRAL_UPDATE_FAILED | customerId={} | referralCode={} | error={}",
                        customerId,
                        referralCodeUsed,
                        ex.getMessage(),
                        ex
                );
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
                String profilePicUrl = s3ImageService.uploadFile(profilePic, "customerProfilePic"+requestDto.getCustomerId());
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
    public CoCustomerWalletResponseDto getCustomerWallet(Integer customerId) {

        log.info("GET_CUSTOMER_WALLET_API_START | customerId={}", customerId);

        CoCustomerWallet wallet = walletRepository.findByCustomerCustomerId(customerId)
                .orElseThrow(() -> new CoBusinessException(COConstants.WALLET_NOT_FOUND));

        CoCustomerWalletResponseDto response = new CoCustomerWalletResponseDto();
        response.setWalletId(wallet.getWalletId());
        response.setCustomerId(wallet.getCustomer().getCustomerId());
        response.setBalancePoints(wallet.getBalancePoints());
        response.setBalanceAmount(wallet.getBalanceAmount());

        log.info("GET_CUSTOMER_WALLET_API_SUCCESS | customerId={}", customerId);

        return response;
    }

    @Override
    public List<CoWalletTransactionHistoryDto> getWalletTransactionHistory(Integer customerId) {

        log.info("GET_WALLET_TRANSACTION_HISTORY_API_START | customerId={}", customerId);

        CoCustomerWallet wallet = walletRepository.findByCustomerCustomerId(customerId)
                .orElseThrow(() -> new CoBusinessException(COConstants.WALLET_NOT_FOUND));

        List<CoWalletTransactionHistoryDto> transactions = transactionsRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getWalletId()).stream()
                .map(transaction -> {
                    CoWalletTransactionHistoryDto dto = new CoWalletTransactionHistoryDto();
                    dto.setTransactionType(transaction.getTransactionType());
                    dto.setPoints(transaction.getPoints());
                    dto.setAmount(transaction.getAmount());
                    dto.setCreatedAt(transaction.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("GET_WALLET_TRANSACTION_HISTORY_API_SUCCESS | customerId={}", customerId);

        return transactions;
    }

    @Override
    public List<CoProfileIncompleteCustomer> getProfileIncompleteCustomers() {

        log.info("Received request to fetch customers with incomplete profiles.");

        List<CoProfileIncompleteCustomer> customers = customerRepository.findAll().stream()
                .filter(customer -> customer.getProfilePicUrl() == null || customer.getProfilePicUrl().isBlank())
                .map(customer -> {
                    CoProfileIncompleteCustomer dto = new CoProfileIncompleteCustomer();
                    dto.setCustomerId(customer.getCustomerId());
                    dto.setFirstName(customer.getFirstName());
                    dto.setLastName(customer.getLastName());
                    dto.setEmail(customer.getEmail());
                    dto.setPhoneNumber(customer.getPhoneNumber());
                    return dto;
                })
                .collect(Collectors.toList());

        return customers;
    }
}