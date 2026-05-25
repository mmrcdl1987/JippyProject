package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.*;
import com.jippy.customerandorder.exception.CoBusinessException;
import com.jippy.customerandorder.iservice.ICoCustomerService;
import com.jippy.customerandorder.mapper.CoCustomerMapper;
import com.jippy.customerandorder.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jippy.customerandorder.exception.CoBadRequestException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

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


    // CREATE CUSTOMER
    @Override
    public CoCustomer createCustomer(CoCustomerRequestDto dto) {

        log.info("Customer creation started");

        // CHECK EMAIL

        Optional<CoCustomer> existingEmail = customerRepository.findByEmail(dto.getEmail());

        if (existingEmail.isPresent()) {

            log.error("Email already exists");

            throw new CoBadRequestException(COConstants.EMAIL_ALREADY_EXISTS);
        }

        // CHECK PHONE

        Optional<CoCustomer> existingPhone = customerRepository.findByPhoneNumber(dto.getPhoneNumber());
        if (existingPhone.isPresent()) {
            log.error("Phone number already exists");
            throw new CoBadRequestException(COConstants.PHONE_ALREADY_EXISTS);
        }

        // SAVE CUSTOMER

        CoCustomer customer = CoCustomerMapper.mapToCustomer(dto);
        CoCustomer savedCustomer = customerRepository.save(customer);
        log.info("Customer saved successfully");

        // FETCH WALLET SETTINGS

        CoWalletSettings walletSettings = walletSettingsRepository.findByPointsType(COConstants.WELCOME_POINTS).orElseThrow(() -> {
            log.error("WELCOME_POINTS not configured");

            return new CoBusinessException(COConstants.WELCOME_POINTS_NOT_CONFIGURED);
        });

        // CREATE WALLET

        CoCustomerWallet wallet = CoCustomerMapper.mapToWallet(savedCustomer, walletSettings.getNumOfPoints(), dto.getCreatedBy());
        walletRepository.save(wallet);
        log.info("Wallet created successfully");

        // SAVE WELCOME TRANSACTION

        CoCustomerWalletTransactions transaction = new CoCustomerWalletTransactions();
        transaction.setWalletId(wallet.getWalletId());
        transaction.setPointsType(COConstants.WELCOME_POINTS);
        transaction.setPoints(walletSettings.getNumOfPoints());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setCreatedBy(dto.getCreatedBy());
        transactionsRepository.save(transaction);

        log.info("Welcome points transaction saved");

        return savedCustomer;
    }


    // CONVERT POINTS

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
        transaction.setPointsType(COConstants.POINTS_CONVERTED);
        transaction.setPoints(-(eligibleBlocks * COConstants.MINIMUM_POINTS_REQUIRED));
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


//    // DAILY STREAK
//
//    @Override
//    public CoCustomerStreakResponseDto updateDailyStreak(Integer customerId) {
//        log.info("Daily streak started");
//        LocalDate today = LocalDate.now();
//        CoCustomerStreak lastStreak = streakRepository.findTopByCustomerIdOrderByCheckInDateDesc(customerId).orElse(null);
//        CoWalletSettings streakSettings = walletSettingsRepository.findByPointsType(COConstants.DAILY_STREAK_POINTS).orElseThrow(() -> new CoBusinessException(COConstants.STREAK_SETTINGS_NOT_FOUND));
//        Integer streakPoints = streakSettings.getNumOfPoints();
//        Integer streakDays = streakSettings.getStreakMinDays();
//        CoCustomerStreak streak;
//
//        // FIRST TIME CHECK-IN
//
//        if (lastStreak == null) {
//            streak = new CoCustomerStreak();
//            streak.setCustomerId(customerId);
//            streak.setCheckInDate(today);
//            streak.setCurrentStreak(1);
//            streak.setMaxStreak(1);
//            streak.setPoints(streakPoints);
//            streak.setCreatedBy(1);
//            streak = streakRepository.save(streak);
//
//            log.info("First streak created");
//
//        } else {
//
//            // ALREADY CHECKED TODAY
//
//            if (lastStreak.getCheckInDate().equals(today)) {
//                throw new CoBusinessException(COConstants.ALREADY_CHECKED_IN);
//            }
//
//            Integer currentStreak;
//            Integer totalPoints;
//            Integer maxStreak;
//
//            // CONTINUE STREAK
//
//            if (lastStreak.getCheckInDate().plusDays(1).equals(today)) {
//
//                // START NEW CYCLE AFTER 15TH DAY
//
//                if (lastStreak.getCurrentStreak() >= streakDays) {
//                    currentStreak = 1;
//                    totalPoints = streakPoints;
//                    maxStreak = lastStreak.getMaxStreak();
//                    log.info("New streak cycle started");
//
//                } else {
//
//                    currentStreak = lastStreak.getCurrentStreak() + 1;
//                    totalPoints = lastStreak.getPoints() + streakPoints;
//                    maxStreak = Math.max(currentStreak, lastStreak.getMaxStreak());
//                    log.info("Streak continued");
//                }
//
//            } else {
//
//                // STREAK BROKEN -> RESTART
//
//                currentStreak = 1;
//                totalPoints = streakPoints;
//                maxStreak = lastStreak.getMaxStreak();
//
//                log.info("Streak restarted");
//            }
//
//            // CREATE NEW DAILY ROW
//            streak = new CoCustomerStreak();
//            streak.setCustomerId(customerId);
//            streak.setCheckInDate(today);
//            streak.setCurrentStreak(currentStreak);
//            //streak.setMaxStreak(maxStreak);
//
//            streak.setPoints(totalPoints);
//            streak.setCreatedBy(1);
//            streak = streakRepository.save(streak);
//
//            log.info("New daily streak row inserted");
//        }
//
//        // STREAK COMPLETED
//
//        if (streak.getCurrentStreak() >= streakDays) {
//            CoCustomerWallet wallet = walletRepository.findByCustomerCustomerId(customerId).orElseThrow(() -> new CoBusinessException(COConstants.WALLET_NOT_FOUND));
//
//            Integer existingPoints = wallet.getBalancePoints() != null ? wallet.getBalancePoints() : 0;
//
//            // ADD STREAK POINTS TO WALLET
//
//            wallet.setBalancePoints(existingPoints + streak.getPoints());
//
//            wallet.setUpdatedAt(LocalDateTime.now());
//
//            wallet.setUpdatedBy(1);
//
//            walletRepository.save(wallet);
//
//            log.info("Wallet updated with streak reward");
//
//            // SAVE TRANSACTION
//
//            CoCustomerWalletTransactions transaction = new CoCustomerWalletTransactions();
//
//            transaction.setWalletId(wallet.getWalletId());
//
//            transaction.setPointsType(COConstants.STREAK_REWARD);
//
//            transaction.setPoints(streak.getPoints());
//
//            transaction.setCreatedAt(LocalDateTime.now());
//
//            transaction.setCreatedBy(1);
//
//            transactionsRepository.save(transaction);
//
//            log.info("Streak reward transaction saved");
//        }
//
//        // RESPONSE
//
//        CoCustomerStreakResponseDto response = new CoCustomerStreakResponseDto();
//
//        response.setSuccess(true);
//
//        response.setMessage(COConstants.STREAK_UPDATED);
//
//        response.setCurrentStreak(streak.getCurrentStreak());
//
//        //response.setMaxStreak(streak.getMaxStreak());
//
//        response.setPoints(streak.getPoints());
//
//        return response;
//    }
    // DAILY STREAK

    @Override
    public CoCustomerStreakResponseDto updateDailyStreak(
            Integer customerId,
            LocalDate date
    ) {

        log.info("Daily streak started");

        LocalDate today =
                date != null
                        ? date
                        : LocalDate.now();

        CoCustomerStreak lastStreak =
                streakRepository
                        .findTopByCustomerIdOrderByCheckInDateDesc(customerId)
                        .orElse(null);

        // FETCH SETTINGS

        CoWalletSettings streakSettings =
                walletSettingsRepository
                        .findByPointsType(COConstants.DAILY_STREAK_POINTS)
                        .orElseThrow(() ->
                                new CoBusinessException(
                                        COConstants.STREAK_SETTINGS_NOT_FOUND
                                ));

        // POINTS PER DAY
        Integer streakPoints =
                streakSettings.getNumOfPoints();

        // MAX STREAK DAYS FROM SETTINGS TABLE
        Integer streakDays =
                streakSettings.getStreakMinDays();

        CoCustomerStreak streak;

        // FIRST TIME CHECK-IN

        if (lastStreak == null) {

            streak = new CoCustomerStreak();

            streak.setCustomerId(customerId);

            streak.setCheckInDate(today);

            // CURRENT STREAK
            streak.setCurrentStreak(1);

            // MAX STREAK FROM SETTINGS TABLE
            streak.setMaxStreak(streakDays);

            // DAY 1 = 25
            streak.setPoints(streakPoints);

            streak.setCreatedBy(1);

            streak = streakRepository.save(streak);

            log.info("First streak created");

        } else {

            // ALREADY CHECKED TODAY

            if (lastStreak.getCheckInDate().equals(today)) {

                throw new CoBusinessException(
                        COConstants.ALREADY_CHECKED_IN
                );
            }

            Integer currentStreak;

            Integer totalPoints;

            Integer maxStreak;

            // CONTINUE STREAK

            if (lastStreak.getCheckInDate()
                    .plusDays(1)
                    .equals(today)) {

                // RESET AFTER MAX DAYS

                if (lastStreak.getCurrentStreak()
                        >= streakDays) {

                    currentStreak = 1;

                    totalPoints = streakPoints;

                    // ALWAYS FROM SETTINGS TABLE
                    maxStreak = streakDays;

                    log.info("New streak cycle started");

                } else {

                    currentStreak =
                            lastStreak.getCurrentStreak() + 1;

                    /*
                     * DAY 1 = 25
                     * DAY 2 = 50
                     * DAY 3 = 75
                     * DAY 4 = 100
                     */

                    totalPoints =
                            currentStreak * streakPoints;

                    // ALWAYS FROM SETTINGS TABLE
                    maxStreak = streakDays;

                    log.info("Streak continued");
                }

            } else {

                // STREAK BROKEN -> RESTART

                currentStreak = 1;

                totalPoints = streakPoints;

                // ALWAYS FROM SETTINGS TABLE
                maxStreak = streakDays;

                log.info("Streak restarted");
            }

            // CREATE NEW DAILY ROW

            streak = new CoCustomerStreak();

            streak.setCustomerId(customerId);

            streak.setCheckInDate(today);

            // SAVE CURRENT STREAK
            streak.setCurrentStreak(currentStreak);

            // SAVE MAX STREAK FROM SETTINGS TABLE
            streak.setMaxStreak(maxStreak);

            // SAVE POINTS
            streak.setPoints(totalPoints);

            streak.setCreatedBy(1);

            streak = streakRepository.save(streak);

            log.info("New daily streak row inserted");
        }

        // ADD TO WALLET WHEN STREAK COMPLETES

        if (streak.getCurrentStreak() >= streakDays) {

            CoCustomerWallet wallet =
                    walletRepository
                            .findByCustomerCustomerId(customerId)
                            .orElseThrow(() ->
                                    new CoBusinessException(
                                            COConstants.WALLET_NOT_FOUND
                                    ));

            Integer existingPoints =
                    wallet.getBalancePoints() != null
                            ? wallet.getBalancePoints()
                            : 0;

            // ADD STREAK REWARD

            wallet.setBalancePoints(
                    existingPoints + streak.getPoints()
            );

            wallet.setUpdatedAt(LocalDateTime.now());

            wallet.setUpdatedBy(1);

            walletRepository.save(wallet);

            log.info("Wallet updated with streak reward");

            // SAVE TRANSACTION

            CoCustomerWalletTransactions transaction =
                    new CoCustomerWalletTransactions();

            transaction.setWalletId(wallet.getWalletId());

            transaction.setPointsType(
                    COConstants.STREAK_REWARD
            );

            transaction.setPoints(streak.getPoints());

            transaction.setCreatedAt(LocalDateTime.now());

            transaction.setCreatedBy(1);

            transactionsRepository.save(transaction);

            log.info("Streak reward transaction saved");
        }

        // RESPONSE

        CoCustomerStreakResponseDto response =
                new CoCustomerStreakResponseDto();

        response.setSuccess(true);

        response.setMessage(COConstants.STREAK_UPDATED);

        response.setCurrentStreak(
                streak.getCurrentStreak()
        );

        response.setMaxStreak(
                streak.getMaxStreak()
        );

        response.setPoints(
                streak.getPoints()
        );

        return response;
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

        senderTransaction.setPointsType(COConstants.POINTS_TRANSFERRED);

        senderTransaction.setPoints(-requestDto.getTransferPoints());

        senderTransaction.setCreatedAt(LocalDateTime.now());

        senderTransaction.setCreatedBy(requestDto.getCreatedBy());

        transactionsRepository.save(senderTransaction);

        log.info("Sender transaction saved");

        // RECEIVER TRANSACTION

        CoCustomerWalletTransactions receiverTransaction = new CoCustomerWalletTransactions();

        receiverTransaction.setWalletId(receiverWallet.getWalletId());

        receiverTransaction.setPointsType(COConstants.POINTS_RECEIVED);

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
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setUpdatedBy(requestDto.getCreatedBy());

        try {

            log.info("UPDATE_CUSTOMER_DB_SAVE_START | customerId={}", customerId);

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
}

