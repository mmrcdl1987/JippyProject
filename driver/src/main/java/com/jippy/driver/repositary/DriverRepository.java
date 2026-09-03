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

    // ================================================================
// UPDATE DRIVER READY TO ACCEPT ORDERS STATUS
// ================================================================
//
// Updates only the ready_to_accept_orders column.
//
// driverId             -> Identifies the driver
// readyToAcceptOrders  -> New value (true / false)
//
// Returns:
// 1 -> Driver updated successfully
// 0 -> No driver found
// ================================================================

    @Modifying
    @Query("""
        UPDATE Driver d
        SET d.readyToAcceptOrders = :readyToAcceptOrders
        WHERE d.driverId = :driverId
        """)
    int updateReadyToAcceptOrders(
            @Param("driverId") Integer driverId,
            @Param("readyToAcceptOrders") Boolean readyToAcceptOrders
    );
}