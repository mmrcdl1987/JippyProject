package com.jippy.division.repositary;

import com.jippy.division.entity.DivOutletWeeklySettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DivOutletWeeklySettlementRepository extends JpaRepository<DivOutletWeeklySettlement, Integer> {


//    SELECT *
// FROM outlet_weekly_settlement WHERE payment_status='PAID' AND email_status='PENDING'
    List<DivOutletWeeklySettlement> findByPaymentStatusAndEmailStatus
            (String paymentStatus, String emailStatus);
}