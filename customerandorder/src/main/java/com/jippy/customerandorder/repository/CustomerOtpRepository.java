package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.entity.CustomerOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOtpRepository extends JpaRepository<CustomerOtp, Long> {

    Optional<CustomerOtp> findTopByCustomerOrderByCustomerOtpIdDesc(CoCustomer customer);

    Optional<CustomerOtp> findTopByCustomerAndIsUsedFalseOrderByCustomerOtpIdDesc(CoCustomer customer);

    List<CustomerOtp> findByCustomer(CoCustomer customer);

    long countByCustomerAndCreatedAtAfter(CoCustomer customer, LocalDateTime createdAt);

    long countByCustomerAndCreatedAtBetween(CoCustomer customer, LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByCustomerAndIsUsedFalseAndExpiresAtAfter(CoCustomer customer, LocalDateTime now);

    void deleteByCreatedAtBefore(LocalDateTime dateTime);
}