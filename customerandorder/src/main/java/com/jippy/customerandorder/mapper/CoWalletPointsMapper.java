package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.CoSaveWalletPointsResponseDTO;
import com.jippy.customerandorder.dto.CoWalletPointsEvent;
import com.jippy.customerandorder.entity.CoCustomerWallet;
import com.jippy.customerandorder.entity.CoCustomerWalletTransactions;
import com.jippy.customerandorder.entity.CoOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CoWalletPointsMapper {

    // Private constructor prevents object creation
    private CoWalletPointsMapper() {
    }

    /**
     * Creates a wallet transaction entity from the updated wallet.
     * <p>
     * This method is responsible only for mapping the wallet data
     * into the transaction history entity.
     *
     * @param wallet updated customer wallet
     * @return wallet transaction entity
     */
    public static CoCustomerWalletTransactions toTransaction(
            CoCustomerWallet wallet,
            CoOrder order,
            Integer transactionPoints) {

        CoCustomerWalletTransactions transaction =
                new CoCustomerWalletTransactions();

        // Wallet information
        transaction.setWalletId(wallet.getWalletId());

        // Order that generated this transaction
        transaction.setOrderId(order.getOrderId());

        // Points earned from this order
        transaction.setPoints(transactionPoints);

        // Transaction type
        transaction.setPointsType(
                COConstants.ORDER_VALUE_POINTS);

        // Audit information
        transaction.setCreatedAt(LocalDateTime.now());
//        transaction.setCreatedBy(order.getCustomerId());

        return transaction;
    }

    /**
     * Converts order, wallet and transaction information
     * into the API response DTO.
     * <p>
     * This method contains only mapping logic and does not
     * perform any business validation or calculation.
     *
     * @param order                 order information
     * @param wallet                updated customer wallet
     * @param transaction           saved transaction history
     * @param orderAmountDiscounted discounted order amount
     * @param previousPoints        wallet points before the update
     * @return wallet points response DTO
     */
    public static CoSaveWalletPointsResponseDTO toResponse
    (CoOrder order, CoCustomerWallet wallet, CoCustomerWalletTransactions transaction,
     BigDecimal orderAmountDiscounted, Integer previousPoints) {

        CoSaveWalletPointsResponseDTO response = new CoSaveWalletPointsResponseDTO();

        // Order and customer information
        response.setOrderId(order.getOrderId());
        response.setCustomerId(order.getCustomerId());
        response.setWalletId(wallet.getWalletId());

        // Wallet points information
        response.setOrderAmountDiscounted(orderAmountDiscounted);

        response.setPreviousBalancePoints(previousPoints);

        response.setUpdatedBalancePoints(wallet.getBalancePoints());

        // Transaction information
        response.setTransactionPoints(transaction.getPoints());


        response.setPointsType(transaction.getPointsType());

        // API response status and message
        response.setStatus(COConstants.MSG_SUCCESS);
        response.setMessage("Wallet points saved successfully " +
                "equivalent to order_amount_discounted");

        return response;
    }

    /**
     * Creates Kafka event for wallet points notification.
     */
    public static CoWalletPointsEvent toWalletPointsEvent(
            CoOrder order,
            Integer transactionPoints) {

        CoWalletPointsEvent event = new CoWalletPointsEvent();

        event.setOrderId(order.getOrderId());
        event.setCustomerId(order.getCustomerId());
        event.setTransactionPoints(transactionPoints);
        event.setPointsType(COConstants.ORDER_VALUE_POINTS);
        event.setNotificationType(COConstants.WALLET_POINTS_EARNED);

        return event;
    }
}