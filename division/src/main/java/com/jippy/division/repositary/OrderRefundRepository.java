package com.jippy.division.repositary;

import com.jippy.division.entity.OrderRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface OrderRefundRepository extends JpaRepository<OrderRefund, UUID> {

    Optional<OrderRefund> findByGatewayRefundId(String rzpRefundId);

    @Query(value = "SELECT rt.* FROM jippy_division.refund_transactions rt " +
            "join  jippy_division.payment_transactions  pt " +
            "on rt.payment_transactions_id = pt. payment_transactions_id " +
            "where rt.refund_status =:paymentStatus and pt.payment_method_type =:paymentMethodType ",nativeQuery = true)
    List<OrderRefund> findByStatusAndTransactionPaymentMethodType(@Param("paymentStatus") String paymentStatus,
            @Param("paymentMethodType") String paymentMethodType);

    @Query(value = "SELECT rt.* FROM  refund_transactions rt WHERE rt.refund_transactions_id = CAST(:refundId AS string)", nativeQuery = true)
    Optional<OrderRefund> findByRefundTransactionsId(@Param("refundId") String refundId);

    List<OrderRefund> findByRefundStatus(String paymentStatusRefundInitiated);
}
