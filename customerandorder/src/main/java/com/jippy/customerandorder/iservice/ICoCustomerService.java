package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoCustomer;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface ICoCustomerService {

    CoCustomer createCustomer(CoCustomerRequestDto dto);

    void qualifyReferralOnFirstOrder(Integer customerId);

    void processReferralReward(Integer customerId, String orderId);

    CoWalletResponseDto convertPoints(Integer customerId);

    // CoCustomerStreakResponseDto updateDailyStreak(Integer customerId);
//    CoCustomerStreakResponseDto updateDailyStreak(Integer customerId);
    CoCustomerStreakResponseDto updateDailyStreak(Integer customerId, LocalDate date);

    CoWalletTransferResponseDto transferWalletPoints(CoWalletTransferRequestDto requestDto);

    CoCustomerResponseDto getCustomer(Integer customerId);

    CoCustomerResponseDto updateCustomer(Integer customerId, CoCustomerRequestDto requestDto);

    String updateCustomerProfile(CoCustomerRequestDto requestDto, MultipartFile profilePic);

    List<CoCustomerListDto> getAllCustomers();

    CoCustomerWalletResponseDto getCustomerWallet(Integer customerId);

    List<CoWalletTransactionHistoryDto> getWalletTransactionHistory(Integer customerId);
    List<CoProfileIncompleteCustomer> getProfileIncompleteCustomers();

}