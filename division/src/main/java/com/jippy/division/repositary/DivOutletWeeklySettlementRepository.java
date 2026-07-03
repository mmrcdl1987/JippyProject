package com.jippy.division.repositary;

import com.jippy.division.entity.DivOutletWeeklySettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DivOutletWeeklySettlementRepository extends JpaRepository<DivOutletWeeklySettlement, Integer> {

    List<DivOutletWeeklySettlement> findByPaymentStatusAndEmailStatus(
            String paymentStatus,
            String emailStatus);

    List<DivOutletWeeklySettlement> findByOutletId(
            Integer outletId);

    List<DivOutletWeeklySettlement> findByOutletIdIn(
            List<Integer> outletIds);

    List<DivOutletWeeklySettlement> findByOutletIdAndWeekEndDateBetween(
            Integer outletId,
            LocalDate fromDate,
            LocalDate toDate);

    List<DivOutletWeeklySettlement> findByOutletIdInAndWeekEndDateBetween(
            List<Integer> outletIds,
            LocalDate fromDate,
            LocalDate toDate);
}