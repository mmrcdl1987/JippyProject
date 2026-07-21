package com.jippy.division.repositary;

import com.jippy.division.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction> findByApplicationOrderIdAndGatewayOrderIdAndPaymentStatus(String orderId,
            String rzpOrderId, String paymentStatusPending);

    Optional<PaymentTransaction> findByGatewayOrderId(String rzpOrderId);

    Optional<PaymentTransaction> findByApplicationOrderId(String orderId);
}
