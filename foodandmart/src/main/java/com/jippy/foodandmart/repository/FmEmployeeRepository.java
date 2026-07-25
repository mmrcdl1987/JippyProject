package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FmEmployeeRepository extends JpaRepository<FmEmployee, Integer> {
    /**
     * Find employee by email.
      */
    Optional<FmEmployee> findByEmailIgnoreCase(String email);

    /**
     * Check whether employee email already exists.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Check whether employee mobile number already exists.
     */
    boolean existsByMobileNumber(String mobileNumber);
}