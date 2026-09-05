package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.TermsAndConditions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermsAndConditionsRepository
        extends JpaRepository<TermsAndConditions, Long> {

    /*
     * Fetch Terms and Conditions using appType.
     *
     * IgnoreCase makes the database lookup
     * case-insensitive.
     * All will find the customer record.
     */
    Optional<TermsAndConditions> findByAppTypeIgnoreCase(String appType);
}