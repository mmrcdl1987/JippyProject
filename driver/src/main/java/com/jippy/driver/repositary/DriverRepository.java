package com.jippy.driver.repositary;



import com.jippy.driver.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Integer> {
    Optional<Driver> findByEmailIgnoreCase(String email);

    Optional<Driver> findByDriverId(Integer driverId);
    // Check whether the phone number already exists
    boolean existsByPhoneNumber(String phoneNumber);

    // Check whether the email already exists
    boolean existsByEmail(String email);

    /**
     * Updates driver approval status.
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE Driver d
            SET d.isApproved = true
            WHERE d.driverId = :driverId
            """)
    void approveDriver(@Param("driverId") Integer driverId);
}