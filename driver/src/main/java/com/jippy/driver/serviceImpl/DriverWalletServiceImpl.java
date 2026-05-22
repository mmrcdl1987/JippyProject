package com.jippy.driver.serviceImpl;


import com.jippy.driver.dto.DriveOrderDto;
import com.jippy.driver.dto.DriveOrderPriceBreakupDto;
import com.jippy.driver.dto.DriverCodRequestDto;
import com.jippy.driver.dto.DriverCodResponseDto;
import com.jippy.driver.entity.DriverWallet;
import com.jippy.driver.entity.DriverWalletTransactions;
//import com.jippy.driver.entity.CoOrder;
//import com.jippy.driver.entity.CoOrderPriceBreakup;
import com.jippy.driver.exception.CoBusinessException;
import com.jippy.driver.feignClients.COFeignClient;
import com.jippy.driver.feignClients.FMFeignClient;
import com.jippy.driver.mapper.DriverMapper;
import com.jippy.driver.repositary.DriverWalletRepository;
import com.jippy.driver.repositary.DriverWalletTransactionsRepository;
//import com.jippy.driver.repositary.CoOrderPriceBreakupRepository;
//import com.jippy.driver.repositary.CoOrderRepository;
import com.jippy.driver.service.DriverWalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class DriverWalletServiceImpl implements DriverWalletService {

    private static final Logger logger = LoggerFactory.getLogger(DriverWalletServiceImpl.class);

//    @Autowired
//    private CoOrderRepository ordersRepo;
//
//    @Autowired
//    private CoOrderPriceBreakupRepository priceRepo;

    @Autowired
    private DriverWalletRepository walletRepo;

    @Autowired
    private DriverWalletTransactionsRepository txnRepo;

    @Autowired
    private FMFeignClient FmFeignClient;

    @Autowired
    private COFeignClient coFeignClient;

    @Override
    public DriverCodResponseDto processDriverCod(DriverCodRequestDto dto) {

        logger.info("Starting COD processing for driverId: {}", dto.getDriverId());

        // -------------------------------
        // STEP 1: FETCH ORDER from orders table
        // -------------------------------
//        CoOrder order = ordersRepo.findById(dto.getOrderId()).orElseThrow(() -> {
//            logger.error("Order not found: {}", dto.getOrderId());
//            return new CoBusinessException("Order not found");
//        });
        DriveOrderDto order =
                coFeignClient.getOrder(dto.getOrderId());

        if (order == null) {

            logger.error("Order not found: {}", dto.getOrderId());

            throw new CoBusinessException("Order not found");
        }

        // -------------------------------
        // STEP 2: VALIDATE STATUS
        // -------------------------------
        if (!"DELIVERED".equalsIgnoreCase(order.getOrderStatus())) {
            logger.error("Order not delivered: {}", dto.getOrderId());
            throw new CoBusinessException("Order is not delivered");
        }
//   ------------------------------------------------------
        // STEP 2.1: VALIDATE DRIVER MATCH mandatory
// ------------------------------------------------------------
        if (!order.getDriverId().equals(dto.getDriverId())) {
            logger.error("Driver mismatch: orderId {} belongs to driverId {} but request has {}",
                    dto.getOrderId(), order.getDriverId(), dto.getDriverId());

            throw new CoBusinessException("Driver does not match order");
        }

        // -------------------------------
        // STEP 3: CHECK COD PAYMENT
        // -------------------------------
//        In payment mod table 2nd row is COD with id 2, so checking with that
        if (order.getPaymentModeId() != 2) {
            logger.error("Order is not COD: {}", dto.getOrderId());
            throw new CoBusinessException("Order is not COD");
        }

        // -------------------------------
        // STEP 4: FETCH ORDER AMOUNT from price breakup table
        // -------------------------------
//        CoOrderPriceBreakup breakup = priceRepo.findByOrderId(dto.getOrderId());
//
//        if (breakup == null) {
//            logger.error("Price breakup not found: {}", dto.getOrderId());
//            throw new CoBusinessException("Price breakup not found");
//        }
//
//        double orderAmount = breakup.getOrderTotalAmount().doubleValue();


        // STEP 4: FETCH ORDER AMOUNT from customerandorder MS

        DriveOrderPriceBreakupDto breakup =
                coFeignClient.getOrderPriceBreakup(
                        dto.getOrderId());

        if (breakup == null) {

            logger.error("Price breakup not found: {}",
                    dto.getOrderId());

            throw new CoBusinessException(
                    "Price breakup not found");
        }

        double orderAmount =
                breakup.getOrderTotalAmount().doubleValue();

        // ------------------------------------------
        // STEP 5: FETCH DRIVER WALLET from wallet table
        // ------------------------------------------
        DriverWallet wallet = walletRepo.findByDriverId(dto.getDriverId())
                .orElseThrow(() -> {
                    logger.error("Driver wallet not found for driverId: {}", dto.getDriverId());
                    return new CoBusinessException("Driver wallet not found");
                });

//        -----------------------------------------
        // STEP 5.1: CHECK IF DRIVER IS LOCKED
         // ----------------------------------------
        boolean isBlocked = false; // actually for 1000 AMOUNT orders lock is true by default

        if (!wallet.getOrdersLock()) {
            logger.warn("Driver already blocked, but allowing deduction");
            isBlocked = true;
        }

        double currentCod = wallet.getTotalCodAmount() != null
                ? wallet.getTotalCodAmount().doubleValue()
                : 1000;
        // -------------------------------
        // STEP 6: DEDUCTING COD AMOUNT
        // -------------------------------
        double updatedCod = currentCod - orderAmount;

        // Prevent negative values
//        we can uncomment if transaction should only show postitive values like up to 0 only
//        if (updatedCod < 0) {
//            updatedCod = 0;
//        }

        wallet.setTotalCodAmount(BigDecimal.valueOf(updatedCod));

        // -------------------------------
        // STEP 7: LOCK LOGIC
        // ONLY WHEN 0 → UNLOCK
        // OTHERWISE → LOCK
        // -------------------------------
        if (updatedCod > 0) {
            wallet.setOrdersLock(true);
        } else {
            wallet.setOrdersLock(false);   // for 0 and negative values

//            Deactivate driver in FM by calling FMFeignClient.deactivateDriver() method
            // for calling FM service to update users table is_active = 'N'
            // for that driverId when orders lock = false in Co wallet table.
            try {
               FmFeignClient.deactivateDriver(dto.getDriverId());

                logger.info("Driver deactivated in FM for driverId: {}", dto.getDriverId());

            } catch (Exception e) {
                logger.error("Error calling FM service: {}", e.getMessage());
            }
        }

        wallet.setUpdatedAt(LocalDateTime.now());

        // Save wallet
        walletRepo.save(wallet);

        // -------------------------------
        // STEP 8: INSERT TRANSACTION
        // -------------------------------
        DriverWalletTransactions txn = DriverMapper.mapToTransaction
                (wallet.getDriverWalletId(), dto.getOrderId(), orderAmount);

        DriverWalletTransactions savetxn = txnRepo.save(txn);

        // -------------------------------
        // STEP 9: PREPARE RESPONSE
        // -------------------------------
        DriverCodResponseDto response = new DriverCodResponseDto();

//        response.setMessage("COD updated successfully");
        if (updatedCod <= 0 || isBlocked) {
            response.setMessage("Driver is blocked due to COD limit reached");
        } else {
            response.setMessage("COD updated successfully");
        }
        response.setDriverId(dto.getDriverId());
        response.setOrderId(dto.getOrderId());
        response.setDeductedAmount(BigDecimal.valueOf(orderAmount));
        response.setRemainingCodAmount(BigDecimal.valueOf(updatedCod));
        response.setOrdersLock(wallet.getOrdersLock());

        logger.info("COD processed successfully for orderId: {}", dto.getOrderId());

        return response;
    }
}