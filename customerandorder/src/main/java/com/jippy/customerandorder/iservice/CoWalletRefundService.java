package com.jippy.customerandorder.iservice;

import java.math.BigDecimal;

public interface CoWalletRefundService {
    
    /**
     * Process wallet refund based on cancellation type
     * 
     * @param orderId Order ID
     * @param customerId Customer ID
     * @param cancellationType Type of cancellation (CUSTOMER, OUTLET, DRIVER)
     * @return Amount refunded to wallet (0 if no refund applicable)
     */
    BigDecimal processWalletRefund(String orderId, Integer customerId, String cancellationType);
    
    /**
     * Get wallet amount used for an order
     * 
     * @param orderId Order ID
     * @return Wallet amount used
     */
    BigDecimal getWalletAmountUsed(String orderId);
}