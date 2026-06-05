package com.jippy.driver.repositary;

import com.jippy.driver.entity.DriverIncentiveHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverIncentiveHistoryRepository extends JpaRepository<DriverIncentiveHistory, Integer> {

//    fetch record for a driver for a specific date
    Optional<DriverIncentiveHistory> findByDriverIdAndCurrDate
    (Integer driverId, LocalDate currDate);


//    SELECT * FROM jippy_driver.driver_incentive_history
//WHERE driver_id = 1 AND curr_date BETWEEN '2026-06-01' AND  '2026-06-30'(monthly);
//    OR
    // Fetch records between start and end date for a driver
    Page<DriverIncentiveHistory> findByDriverIdAndCurrDateBetween
    (Integer driverId, LocalDate startDate, LocalDate endDate,  Pageable pageable);

    // Fetch ALL records for a driver ID
    Page<DriverIncentiveHistory> findByDriverId(Integer driverId , Pageable pageable);
}