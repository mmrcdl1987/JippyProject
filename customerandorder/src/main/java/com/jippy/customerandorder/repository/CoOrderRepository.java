package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.projection.CoDriverEarningsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

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
    CoDriverEarningsProjection fetchDriverEarnings
    (@Param("driverId") Integer driverId, @Param("date") LocalDate date);

    //
// to fetch Frequent outlets (>=3)
//    for ex: if a customer has ordered from outlet A 5 times, outlet B 2 times,
//    and outlet C 3 times, the query will return outlet A and  outlet C as frequent outlets
//    for that customer, since they have been ordered from at least 3 times.
    @Query(value = """
                SELECT outlet_id
                FROM jippy_customer_and_order.orders
                WHERE customer_id = :customerId
                GROUP BY outlet_id
                HAVING COUNT(*) >= 3
            """, nativeQuery = true)
    List<Integer> findFrequentOutlets(Integer customerId);

    //
    // to fetch Most recent outlet
//    this query retrieves the most recent outlet_id from the orders table for a given customer_id.
//    It orders the results by the created_at timestamp in descending order, ensuring that
//    the most recent order is at the top.
//    The LIMIT 1 clause ensures that only one record (the most recent one) is returned.
    @Query(value = """
                SELECT outlet_id
                FROM jippy_customer_and_order.orders
                WHERE customer_id = :customerId
                ORDER BY created_at DESC
                LIMIT 1
            """, nativeQuery = true)
    Integer findRecentOutlet(Integer customerId);
}