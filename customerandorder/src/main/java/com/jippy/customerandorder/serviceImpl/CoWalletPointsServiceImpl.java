package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.CoSaveWalletPointsRequestDTO;
import com.jippy.customerandorder.dto.CoSaveWalletPointsResponseDTO;
import com.jippy.customerandorder.dto.CoWalletPointsEvent;
import com.jippy.customerandorder.entity.CoCustomerWallet;
import com.jippy.customerandorder.entity.CoCustomerWalletTransactions;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.entity.CoOrderPriceBreakup;
import com.jippy.customerandorder.exception.CoResourceNotFoundException;
import com.jippy.customerandorder.exception.CoValidationException;
import com.jippy.customerandorder.iservice.CoWalletPointsService;
import com.jippy.customerandorder.mapper.CoWalletPointsMapper;
import com.jippy.customerandorder.producer.CoWalletPointsKafkaProducer;
import com.jippy.customerandorder.repository.CoCustomerWalletRepository;
import com.jippy.customerandorder.repository.CoCustomerWalletTransactionsRepository;
import com.jippy.customerandorder.repository.CoOrderPriceBreakupRepository;
import com.jippy.customerandorder.repository.CoOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoWalletPointsServiceImpl
        implements CoWalletPointsService {

    private final CoOrderRepository orderRepository;
    private final CoOrderPriceBreakupRepository priceBreakupRepository;
    private final CoCustomerWalletRepository walletRepository;
    private final CoCustomerWalletTransactionsRepository transactionRepository;

    private final CoWalletPointsKafkaProducer walletPointsKafkaProducer;
    @Override
    @Transactional
    public CoSaveWalletPointsResponseDTO saveWalletPointsEqualToOrderAmountDiscounted(
            CoSaveWalletPointsRequestDTO requestDTO) {

        String orderId = requestDTO.getOrderId();

        log.info(
                "SAVE_WALLET_POINTS_START | orderId={}",
                orderId
        );

        // 1. Fetch order
        CoOrder order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new CoResourceNotFoundException(
                                "Order not found: " + orderId
                        ));

        // 2. Validate order status
        validateDeliveredOrder(order);

        // 3. Prevent duplicate wallet points
        if (transactionRepository.existsByOrderIdAndTransactionType(
                orderId,
                COConstants.ORDER_VALUE_POINTS)) {

            log.warn(
                    "WALLET_POINTS_ALREADY_CREDITED | orderId={}",
                    orderId
            );

            throw new CoValidationException(
                    "Wallet points already credited for Your order: "
                            + orderId
            );
        }

        // 4. Fetch price breakup
        CoOrderPriceBreakup priceBreakup =
                priceBreakupRepository.findByOrderId(orderId);

        if (priceBreakup == null) {

            throw new CoResourceNotFoundException(
                    "Order price breakup not found: " + orderId
            );
        }

        // 5. Get discounted order amount
        BigDecimal orderAmountDiscounted =
                priceBreakup.getOrderAmountDiscounted();

        if (orderAmountDiscounted == null ||
                orderAmountDiscounted.compareTo(
                        BigDecimal.ZERO) <= 0) {

            throw new CoValidationException(
                    "Order amount discounted must be greater than zero"
            );
        }

        // 6. Convert discounted amount into points
        Integer transactionPoints =
                orderAmountDiscounted.intValue();

        // 7. Fetch customer wallet
        CoCustomerWallet wallet =
                walletRepository
                        .findByCustomerCustomerId(
                                order.getCustomerId())
                        .orElseThrow(() ->
                                new CoResourceNotFoundException(
                                        "Wallet not found for customer: "
                                                + order.getCustomerId()
                                ));

        // 8. Get previous wallet balance
        Integer previousPoints =
                wallet.getBalancePoints() == null
                        ? 0
                        : wallet.getBalancePoints();

        // 9. Calculate updated wallet balance
        Integer updatedPoints =
                previousPoints + transactionPoints;

        log.info(
                "WALLET_POINTS_CALCULATED | orderId={} | " +
                        "previousPoints={} | transactionPoints={} | " +
                        "updatedPoints={}",
                orderId,
                previousPoints,
                transactionPoints,
                updatedPoints
        );

        // 10. Update wallet
        updateWallet(
                wallet,
                updatedPoints,
                order.getCustomerId()
        );

        // 11. Save transaction history
        CoCustomerWalletTransactions transaction =
                CoWalletPointsMapper.toTransaction(
                        wallet,
                        order,
                        transactionPoints
                );

        transactionRepository.save(transaction);

        log.info(
                "WALLET_TRANSACTION_SAVED | orderId={} | " +
                        "walletId={} | transactionPoints={}",
                orderId,
                wallet.getWalletId(),
                transactionPoints
        );

        // 12. Create Kafka event
        CoWalletPointsEvent event =
                CoWalletPointsMapper.toWalletPointsEvent(
                        order,
                        transactionPoints
                );

        // 13. Publish event through Kafka producer
        walletPointsKafkaProducer.sendWalletPointsEvent(event);

        log.info(
                "WALLET_POINTS_EVENT_PUBLISHED | " +
                        "orderId={} | customerId={} | points={}",
                orderId,
                order.getCustomerId(),
                transactionPoints
        );

        // 14. Prepare API response
        return CoWalletPointsMapper.toResponse(
                order,
                wallet,
                transaction,
                orderAmountDiscounted,
                previousPoints
        );
    }

//    ================================================================================================
//    ================================ Helper Methods ============================================
//    ================================================================================================

    /**
     * Validates that the order is DELIVERED.
     */
    private void validateDeliveredOrder(CoOrder order) {

        if (!COConstants.DELIVERED.equalsIgnoreCase(
                order.getOrderStatus())) {

            log.warn(
                    "WALLET_POINTS_VALIDATION_FAILED | " +
                            "orderId={} | status={}",
                    order.getOrderId(),
                    order.getOrderStatus()
            );

            throw new CoValidationException(
                    "Your Order is Not Yet DELIVERED. " +
                            "Wallet points can be credited only for DELIVERED orders. " +
                            "Current status: " +
                            order.getOrderStatus()
            );
        }
    }

    /**
     * Updates wallet balance and audit fields.
     */
    private void updateWallet(
            CoCustomerWallet wallet,
            Integer updatedPoints,
            Integer customerId) {

        wallet.setBalancePoints(updatedPoints);
        wallet.setUpdatedAt(LocalDateTime.now());
        wallet.setUpdatedBy(customerId);

        walletRepository.save(wallet);
    }
}
