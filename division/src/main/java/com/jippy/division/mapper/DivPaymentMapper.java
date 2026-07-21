package com.jippy.division.mapper;

import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.dto.DivOrderDto;
import com.jippy.division.dto.DivPlaceOrderRequestDto;
import com.jippy.division.entity.OrderRefund;
import com.jippy.division.entity.PaymentTransaction;

import java.time.LocalDateTime;

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

    public static OrderRefund mapToRefundOrder(DivOrderDto orderDto,
            PaymentTransaction tx, String rzpRefundId, String reason) {

        OrderRefund orderRefund = new OrderRefund();
        orderRefund.setApplicationOrderId(orderDto.getOrderId());
        orderRefund.setPaymentTransaction(tx);
        orderRefund.setGatewayRefundId(rzpRefundId);
        orderRefund.setAmountInPaise(tx.getAmount());
        orderRefund.setStatus(DivAppConstants.PAYMENT_STATUS_REFUND_INITIATED);
        orderRefund.setReason(reason);

        return orderRefund;
    }

}
