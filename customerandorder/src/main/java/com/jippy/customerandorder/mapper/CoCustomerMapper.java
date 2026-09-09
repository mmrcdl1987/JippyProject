package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.entity.CoCustomerStreak;
import com.jippy.customerandorder.entity.CoCustomerWallet;
import com.jippy.customerandorder.entity.CoCustomerWalletTransactions;
import com.jippy.customerandorder.projection.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public static CoCustomerStreak mapToCustomerStreak(Integer customerId, LocalDate checkInDate, Integer currentStreak, Integer points, Integer createdBy) {

        CoCustomerStreak streak = new CoCustomerStreak();

        streak.setCustomerId(customerId);

        streak.setCheckInDate(checkInDate);

        streak.setCurrentStreak(currentStreak);

        streak.setPoints(points);

        streak.setCreatedBy(createdBy);

        return streak;
    }
    // STREAK ENTITY

    public static CoCustomerWalletTransactions mapToWalletTransaction(Integer walletId, String transactionType, Integer points, Integer createdBy) {

        CoCustomerWalletTransactions transaction = new CoCustomerWalletTransactions();

        transaction.setWalletId(walletId);

        transaction.setTransactionType(transactionType);

        transaction.setPoints(points);

        transaction.setCreatedAt(LocalDateTime.now());

        transaction.setCreatedBy(createdBy);

        return transaction;
    }

    // WALLET TRANSACTION

    public static CoCustomerWalletTransactions mapToWalletTransaction(Integer walletId, String transactionType, Integer points, BigDecimal amount, Integer createdBy) {

        CoCustomerWalletTransactions transaction = new CoCustomerWalletTransactions();

        transaction.setWalletId(walletId);

        transaction.setTransactionType(transactionType);

        transaction.setPoints(points);

        transaction.setAmount(amount);

        transaction.setCreatedAt(LocalDateTime.now());

        transaction.setCreatedBy(createdBy);

        return transaction;
    }

    public static CoCustomerStreakResponseDto mapToStreakResponse(Integer currentStreak, Integer points, String message) {

        CoCustomerStreakResponseDto response = new CoCustomerStreakResponseDto();

        response.setSuccess(true);

        response.setMessage(message);

        response.setCurrentStreak(currentStreak);

        response.setPoints(points);

        return response;
    }

    // STREAK RESPONSE

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

    // WALLET RESPONSE

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

    // TRANSFER RESPONSE

    /**
     * Converts order flow count projection into response DTO.
     *
     * @param projection order flow count projection
     * @return complete order flow count DTO
     */
    public static CoCompleteOrdersFlowCountsDto mapToCompleteOrdersFlowCountsDto(CoCompleteOrdersFlowCountsProjection projection) {

        CoCompleteOrdersFlowCountsDto dto = new CoCompleteOrdersFlowCountsDto();

        dto.setTotalOrdersCount(projection.getTotalOrdersCount());
        dto.setOrdersPlaced(projection.getOrdersPlaced());
        dto.setOrdersConfirmed(projection.getOrdersConfirmed());
        dto.setOrdersShipped(projection.getOrdersShipped());
        dto.setOrdersCompleted(projection.getOrdersCompleted());
        dto.setOrdersRejected(projection.getOrdersRejected());

        return dto;
    }

//    ========================================================================================
//    ========================================================================================

    /**
     * Combines CO order details with FM outlet details
     * and Driver details.
     * <p>
     */
    public static List<CoOrderDetailsByOrderStatusDto> mapToCompleteOrderDetails(List<CoOrderDetailsByOrderStatusProjection> projections, List<CoFmOutletDetailsDto> outletDetails, List<CoDriverDetailsDto> driverDetails) {

        List<CoOrderDetailsByOrderStatusDto> response = new ArrayList<>();

        for (CoOrderDetailsByOrderStatusProjection projection : projections) {

            CoOrderDetailsByOrderStatusDto dto = new CoOrderDetailsByOrderStatusDto();

            // ----------------------------------------------------
            // CO information
            // ----------------------------------------------------

            dto.setOrderId(projection.getOrderId());

            dto.setOutletId(projection.getOutletId());

            dto.setDriverId(projection.getDriverId());

            dto.setOrderStatus(projection.getOrderStatus());

            dto.setCustomerName(projection.getCustomerName());

            dto.setOrderAmount(projection.getOrderAmount());

            // ----------------------------------------------------
            // FM information
            // ----------------------------------------------------

            if (projection.getOutletId() != null) {

                for (CoFmOutletDetailsDto outlet : outletDetails) {

                    if (projection.getOutletId().equals(outlet.getOutletId())) {

                        dto.setOutletName(outlet.getOutletName());

                        dto.setAreaName(outlet.getAreaName());

                        break;
                    }
                }
            }

            // ----------------------------------------------------
            // Driver information
            // ----------------------------------------------------

            if (projection.getDriverId() != null) {

                for (CoDriverDetailsDto driver : driverDetails) {

                    if (projection.getDriverId().equals(driver.getDriverId())) {

                        // Set driver name
                        dto.setDriverName(driver.getDriverName());

                        // Set driver mobile number
                        dto.setDriverMobileNumber(driver.getDriverMobileNumber());

                        break;
                    }
                }
            }

            response.add(dto);
        }

        return response;
    }
//    ======================================================================================
//    ======================================================================================

//    /**
//     * Converts order details projection list into DTO list.
//     *
//     * @param projections order details projection list
//     * @return order details DTO list
//     */
//    public static List<CoOrderDetailsByOrderStatusDto>
//    mapToOrderDetailsByOrderStatusDto(
//            List<CoOrderDetailsByOrderStatusProjection> projections) {
//
//        List<CoOrderDetailsByOrderStatusDto> dtoList =
//                new ArrayList<>();
//
//        for (CoOrderDetailsByOrderStatusProjection projection : projections) {
//
//            CoOrderDetailsByOrderStatusDto dto = new CoOrderDetailsByOrderStatusDto();
//
//            dto.setOrderId(projection.getOrderId());
//            dto.setOutletId(projection.getOutletId());
//            dto.setDriverId(projection.getDriverId());
//            dto.setOrderStatus(projection.getOrderStatus());
//
//            dtoList.add(dto);
//        }
//
//        return dtoList;
//    }

    /**
     * Maps order flow count projection into response DTO.
     *
     * <p>
     * The same response is used for merchant, outlet and driver
     * order flow count requests.
     *
     * @param projection order flow count projection
     * @return order flow count response DTO
     */
    public static CoOrderFlowCountForMerchantOutletOrDriverDto mapToOrderFlowCountForMerchantOrOutletOrDriver(CoOrderFlowCountProjection projection) {

        CoOrderFlowCountForMerchantOutletOrDriverDto dto = new CoOrderFlowCountForMerchantOutletOrDriverDto();

        dto.setTotalOrdersCount(projection.getTotalOrdersCount());

        dto.setCompletedOrdersCount(projection.getCompletedOrdersCount());

        dto.setRejectedOrdersCount(projection.getRejectedOrdersCount());

        return dto;
    }

//    ====================================================================================
//    ====================================================================================

    public static CoOrderDetailsOfOutletDto mapToOrderDetailsOfOutlet(CoOrderDetailsOfOutletProjection projection) {

        CoOrderDetailsOfOutletDto dto = new CoOrderDetailsOfOutletDto();

        dto.setOrderId(projection.getOrderId());

        dto.setOutletId(projection.getOutletId());

        dto.setCustomerName(projection.getCustomerName());

        dto.setDriverId(projection.getDriverId());

        dto.setOrderStatus(projection.getOrderStatus());

        dto.setMerchantTotalPrice(projection.getMerchantTotalPrice());

        return dto;
    }

    //    =================================================================================
//    =================================================================================//
    public static CoOrderDetailsOfDriverDto mapToOrderDetailsOfDriver(CoOrderDetailsOfDriverProjection projection) {

        CoOrderDetailsOfDriverDto dto = new CoOrderDetailsOfDriverDto();

        dto.setOrderId(projection.getOrderId());

        dto.setOutletId(projection.getOutletId());

        dto.setCustomerName(projection.getCustomerName());

        dto.setDriverId(projection.getDriverId());

        dto.setOrderStatus(projection.getOrderStatus());

        dto.setPickUpDistanceInKms(projection.getPickUpDistanceInKms());

        dto.setDeliveryDistanceInKms(projection.getDeliveryDistanceInKms());

        dto.setPickUpCharges(projection.getPickUpCharges());

        dto.setDriverDeliveryFee(projection.getDriverDeliveryFee());

        dto.setDriverTotalCharges(projection.getDriverTotalCharges());

        return dto;
    }
//    =================================================================================
//    =================================================================================

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
}