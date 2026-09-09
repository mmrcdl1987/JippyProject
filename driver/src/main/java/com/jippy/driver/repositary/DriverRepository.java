package com.jippy.driver.repositary;


import com.jippy.driver.entity.Driver;
import com.jippy.driver.projection.DriverDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Integer> {
    Optional<Driver> findByEmailIgnoreCase(String email);

    Optional<Driver> findByDriverId(Integer driverId);
    // Check whether the phone number already exists
    boolean existsByPhoneNumber(String phoneNumber);

    // Check whether the email already exists
    boolean existsByEmail(String email);

    Optional<Driver> findByPhoneNumber(String phoneNumber);

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
//    ======================================================================================
//    ======================================================================================
    /**
     * Fetches driver ID and full driver name for multiple drivers.
     */
    @Query(value = """
    SELECT
        d.driver_id AS "driverId",

        CONCAT(
            COALESCE(d.first_name, ''),
            ' ',
            COALESCE(d.last_name, '')
        ) AS "driverName",

        d.phone_number AS "driverMobileNumber"
    
        FROM "jippy_driver"."driver" d
    
        WHERE d.driver_id IN (:driverIds)
        """,
                nativeQuery = true)
    List<DriverDetailsProjection> getDriverDetailsByIds(
            @Param("driverIds") List<Integer> driverIds
    );
}