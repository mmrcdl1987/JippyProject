package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomerStreak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface CoCustomerStreakRepository extends JpaRepository<CoCustomerStreak, Integer> {

    Optional<CoCustomerStreak> findTopByCustomerIdOrderByCheckInDateDesc(Integer customerId);

    boolean existsByCustomerIdAndCheckInDate(Integer customerId, LocalDate checkInDate);

    void deleteByCustomerId(Integer customerId);
}