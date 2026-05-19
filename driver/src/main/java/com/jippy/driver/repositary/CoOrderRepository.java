package com.jippy.driver.repositary;


import com.jippy.driver.entity.CoOrder;
import com.jippy.driver.projection.DriverEarningsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface CoOrderRepository extends JpaRepository<CoOrder, String> {

    // Calculate total earnings for driver on particular date + count of orders to projection
//    this query calculates the total earnings for a specific driver on a given date by summing up the total_delivery_fee, surge_fee, and tips for all orders that match the driver_id and created_at date. The COALESCE function is used to handle cases where surge_fee or tips might be null, treating them as 0 in the sum.
//    The final result is also wrapped in a COALESCE to return 0 if there are no matching orders.
// used by projection interface CoDriverEarningsProjection to fetch total earnings
// and count of orders for a driver on a specific date
    @Query(value = """
            SELECT
                COALESCE(
                    SUM(
                        o.total_delivery_fee
                        + COALESCE(o.surge_fee, 0)
                        + COALESCE(o.tips, 0)
                    ),
                    0
                ) AS totalEarnings,
            
                COUNT(o.driver_order_id) AS ordersCount
            
            FROM jippy_customer_and_order.driver_orders o
            
            WHERE o.driver_id = :driverId
            AND DATE(o.created_at) = :date
            """, nativeQuery = true)
    DriverEarningsProjection fetchDriverEarnings
    (@Param("driverId") Integer driverId, @Param("date") LocalDate date);
}