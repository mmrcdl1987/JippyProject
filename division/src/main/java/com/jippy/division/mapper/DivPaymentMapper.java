package com.jippy.division.mapper;

import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.dto.DivOrderDto;
import com.jippy.division.dto.DivPlaceOrderRequestDto;
import com.jippy.division.entity.OrderRefund;
import com.jippy.division.entity.PaymentTransaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DivPaymentMapper {

    public static PaymentTransaction mapToTransactions(DivOrderDto orderDto,
            String rzpOrderId, int amountInPaise) {

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setApplicationOrderId(orderDto.getOrderId());
        transaction.setGatewayOrderId(rzpOrderId);
        transaction.setAmount(amountInPaise);
        transaction.setPaymentStatus(DivAppConstants.PAYMENT_STATUS_PENDING);
        transaction.setCurrency(DivAppConstants.CURRENCY);
        transaction.setPaymentMethodType(orderDto.getPaymentModeId());
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }

    public static OrderRefund mapToRefundOrder(String orderId,
            PaymentTransaction tx, String gatewayRefundId, String reason,UUID refundTxnId) {

        OrderRefund orderRefund = new OrderRefund();
        orderRefund.setApplicationOrderId(orderId);
        orderRefund.setPaymentTransaction(tx);
        orderRefund.setRefundTransactionsId(refundTxnId);
        orderRefund.setAmountInPaise(tx.getAmount());
        orderRefund.setReason(reason);
        orderRefund.setGatewayRefundId(gatewayRefundId);

        return orderRefund;
    }

}
