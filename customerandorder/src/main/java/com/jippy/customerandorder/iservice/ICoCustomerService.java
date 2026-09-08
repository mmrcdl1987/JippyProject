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

    // ================================================================
    // UPDATE CUSTOMER PROFILE PICTURE
    // ================================================================

    String updateCustomerProfilePic(CustomerProfilePicDto customerDto);

    /**
     * Fetches complete order flow counts based on order status.
     *
     * @return complete order flow counts
     */
    CoCompleteOrdersFlowCountsDto getCompleteOrdersFlowCounts();

    /**
     * Fetches order details based on order status.
     *
     * @param orderStatus order status used for filtering
     * @return list of matching order details
     */
    List<CoOrderDetailsByOrderStatusDto> getCompleteOrdersDetailsByOrderStatus
        (String orderStatus);

    //    =================================================================================
//    =================================================================================
    CoOrderCompleteDetailsResponseDto getOrderCompleteDetails(String orderId);

//    ====================================================================================
    /**
     * Fetches total, completed and rejected order counts
     * for a merchant or outlet.
     *
     * @param merchantId optional merchant ID
     * @param outletId optional outlet ID
     * @return order flow counts
     */
    CoOrderFlowCountForMerchantOrOutletDto getOrderFlowCountForMerchantOrOutlet(
            Integer merchantId, Integer outletId);
}