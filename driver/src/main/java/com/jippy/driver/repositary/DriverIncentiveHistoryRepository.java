package com.jippy.driver.repositary;

import com.jippy.driver.entity.DriverIncentiveHistory;
import com.jippy.driver.projection.DriverIncentiveDetailProjection;
import com.jippy.driver.projection.DriverIncentiveSettlementProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverIncentiveHistoryRepository extends JpaRepository<DriverIncentiveHistory, Integer> {

    //    fetch record for a driver for a specific date
    Optional<DriverIncentiveHistory> findByDriverIdAndCurrDate(Integer driverId, LocalDate currDate);


    //    SELECT * FROM jippy_driver.driver_incentive_history
//WHERE driver_id = 1 AND curr_date BETWEEN '2026-06-01' AND  '2026-06-30'(monthly);
//    OR
    // Fetch records between start and end date for a driver
    Page<DriverIncentiveHistory> findByDriverIdAndCurrDateBetween(Integer driverId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Fetch ALL records for a driver ID
    Page<DriverIncentiveHistory> findByDriverId(Integer driverId, Pageable pageable);

//    -----------------------------------------------------------------------------------------------------------------

//        for api ->getDriversIncentivesForSettlements?filter=currentMonth
    //     Fetch total incentive amount grouped by driver ID for all drivers within the specified
//     date range. The result includes driver ID and the total incentive amount for each driver.
    @Query(value = """
            SELECT
                driver_id AS driverId,
                SUM(incentive_amount) AS totalIncentivesAmount
            FROM jippy_driver.driver_incentive_history
            WHERE curr_date BETWEEN :startDate AND :endDate
            GROUP BY driver_id
            ORDER BY driver_id
            """, nativeQuery = true)
    List<DriverIncentiveSettlementProjection> getDriverIncentiveSettlementCalculation
    (@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    //     for api ->getDriversIncentivesForSettlements?filter=currentMonth
    //     Fetch incentive details for every driver. This includes driver ID, date,
    //     incentive amount,and the count of completed orders for each record
    //     within the specified date range.
    @Query(value = """
            SELECT
                driver_id AS driverId,
                curr_date AS currDate,
                incentive_amount AS incentiveAmount,
                completed_orders_count AS completedOrdersCount
            FROM jippy_driver.driver_incentive_history
            WHERE curr_date BETWEEN :startDate AND :endDate
            ORDER BY driver_id,curr_date
            """, nativeQuery = true)
    List<DriverIncentiveDetailProjection> getDriverIncentiveDetails
    (@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}