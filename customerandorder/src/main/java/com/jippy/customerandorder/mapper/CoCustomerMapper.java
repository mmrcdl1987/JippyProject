package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoCustomerRequestDto;
import com.jippy.customerandorder.dto.CoCustomerResponseDto;
import com.jippy.customerandorder.dto.CoCustomerStreakResponseDto;
import com.jippy.customerandorder.dto.CoWalletTransferResponseDto;
import com.jippy.customerandorder.entity.*;
import org.springframework.stereotype.Component;
import com.jippy.customerandorder.entity.CoCustomerWallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jippy.customerandorder.dto.CoCustomerStreakResponseDto;
import com.jippy.customerandorder.dto.CoWalletResponseDto;
import com.jippy.customerandorder.dto.CoWalletTransferResponseDto;

import com.jippy.customerandorder.entity.CoCustomerStreak;
import com.jippy.customerandorder.entity.CoCustomerWallet;
import com.jippy.customerandorder.entity.CoCustomerWalletTransactions;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class CoCustomerMapper {

    private CoCustomerMapper() {
    }
    // CUSTOMER MAPPER


    public static CoCustomer mapToCustomer(CoCustomerRequestDto dto) {

        CoCustomer customer = new CoCustomer();

        customer.setFirstName(dto.getFirstName());

        customer.setLastName(dto.getLastName());

        customer.setEmail(dto.getEmail());

        customer.setPhoneNumber(dto.getPhoneNumber());

        customer.setReferralCode(generateReferral(

                dto.getFirstName(),

                dto.getLastName(),

                dto.getPhoneNumber()));

        customer.setUsedReferral(dto.getReferralCodeUsed());

        customer.setCreatedAt(LocalDateTime.now());

        customer.setCreatedBy(dto.getCreatedBy());

        return customer;
    }


    // WALLET MAPPER


    public static CoCustomerWallet mapToWallet(CoCustomer customer, Integer balancePoints, Integer createdBy) {

        CoCustomerWallet wallet = new CoCustomerWallet();

        wallet.setCustomer(customer);
        wallet.setBalancePoints(balancePoints);
        wallet.setBalanceAmount(BigDecimal.ZERO);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setCreatedBy(createdBy);
        return wallet;
    }


    // REFERRAL CODE GENERATOR


    public static String generateReferral(String firstName, String lastName, String phoneNumber) {

        String firstPart = firstName.substring(0, Math.min(3, firstName.length()));
        String lastPart = lastName.substring(0, Math.min(2, lastName.length()));
        String phoneLast3 = phoneNumber.substring(phoneNumber.length() - 3);

        return (firstPart + phoneLast3 + lastPart).toUpperCase();
    }



    public CoCustomerResponseDto mapToResponse(CoCustomer customer) {

        CoCustomerResponseDto dto = new CoCustomerResponseDto();

        dto.setCustomerId(customer.getCustomerId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setDOB(customer.getDateOfBirth());
        dto.setProfilePicUrl(customer.getProfilePicUrl());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setReferralCode(customer.getReferralCode());
        dto.setCustomerStatusId(customer.getCustomerStatus().getCustomerStatusId());

        return dto;
    }
    // STREAK ENTITY

    public static CoCustomerStreak mapToCustomerStreak(Integer customerId, LocalDate checkInDate, Integer currentStreak, Integer points, Integer createdBy) {

        CoCustomerStreak streak = new CoCustomerStreak();

        streak.setCustomerId(customerId);

        streak.setCheckInDate(checkInDate);

        streak.setCurrentStreak(currentStreak);

        streak.setPoints(points);

        streak.setCreatedBy(createdBy);

        return streak;
    }

    // WALLET TRANSACTION

    public static CoCustomerWalletTransactions mapToWalletTransaction(Integer walletId, String pointsType, Integer points, Integer createdBy) {

        CoCustomerWalletTransactions transaction = new CoCustomerWalletTransactions();

        transaction.setWalletId(walletId);

        transaction.setPointsType(pointsType);

        transaction.setPoints(points);

        transaction.setCreatedAt(LocalDateTime.now());

        transaction.setCreatedBy(createdBy);

        return transaction;
    }

    // STREAK RESPONSE

    public static CoCustomerStreakResponseDto mapToStreakResponse(Integer currentStreak, Integer points, String message) {

        CoCustomerStreakResponseDto response = new CoCustomerStreakResponseDto();

        response.setSuccess(true);

        response.setMessage(message);

        response.setCurrentStreak(currentStreak);

        response.setPoints(points);

        return response;
    }

    // WALLET RESPONSE

    public static CoWalletResponseDto mapToWalletResponse(CoCustomerWallet wallet, String message) {

        CoWalletResponseDto response = new CoWalletResponseDto();

        response.setSuccess(true);

        response.setMessage(message);

        response.setWalletId(wallet.getWalletId());

        response.setCustomerId(wallet.getCustomer().getCustomerId());

        response.setBalancePoints(wallet.getBalancePoints());

        response.setBalanceAmount(wallet.getBalanceAmount());

        return response;
    }

    // TRANSFER RESPONSE

    public static CoWalletTransferResponseDto mapToTransferResponse(Integer senderCustomerId, Integer receiverCustomerId, Integer transferredPoints, Integer senderRemainingPoints, String message) {

        CoWalletTransferResponseDto response = new CoWalletTransferResponseDto();

        response.setSuccess(true);

        response.setMessage(message);

        response.setSenderCustomerId(senderCustomerId);

        response.setReceiverCustomerId(receiverCustomerId);

        response.setTransferredPoints(transferredPoints);

        response.setSenderRemainingPoints(senderRemainingPoints);

        return response;
    }
}