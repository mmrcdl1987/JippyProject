package com.jippy.driver.repositary;

import com.jippy.driver.entity.DriverOrder;
import com.jippy.driver.projection.DriverOrderHistoryProjection;
import com.jippy.driver.projection.DriverOrderSettlementProjection;
import com.jippy.driver.projection.DriverSettlementProjection;
import com.jippy.driver.projection.DriverTotalEarningsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DriverOrderRepository extends JpaRepository<DriverOrder, Integer> {

    //        @Query(value = """
//                SELECT
//
//                    d.driver_id AS driverId,
//
//                    d.order_id AS orderId,
//
//                    d.pick_up_distance_in_kms AS pickUpDistanceInKms,
//
//                    d.delivery_distance_in_kms AS deliveryDistanceInKms,
//
//                    d.pick_up_charges AS pickUpCharges,
//
//                    d.deliver_charges AS deliverCharges,
//
//                    d.total_delivery_fee AS totalDeliveryFee,
//
//                    d.surge_fee AS surgeFee,
//
//                    d.tips AS tips,
//
//                    o.order_status AS orderStatus,
//
//                    o.outlet_id AS outletId
//
//                FROM jippy_driver.driver_orders d
//
//                JOIN jippy_customer_and_order.orders o
//                     ON d.order_id = o.order_id
//
//                WHERE d.driver_id = :driverId
//                """, nativeQuery = true)
//        List<DriverOrderHistoryProjection> fetchOrderEarningsHistory(
//                @Param("driverId") Integer driverId);
    @Query(value = """
            SELECT
            
                d.driver_id AS driverId,
                d.order_id AS orderId,
                d.pick_up_distance_in_kms AS pickUpDistanceInKms,
                d.delivery_distance_in_kms AS deliveryDistanceInKms,
                d.pick_up_charges AS pickUpCharges,
                d.deliver_charges AS deliverCharges,
                d.total_delivery_fee AS totalDeliveryFee,
                d.surge_fee AS surgeFee,
                d.tips AS tips,
                d.created_at AS createdAt
            
            FROM jippy_driver.driver_orders d
            
            WHERE d.driver_id = :driverId
            """, nativeQuery = true)
    List<DriverOrderHistoryProjection> fetchOrderEarningsHistory(@Param("driverId") Integer driverId);

    @Query(value = """
            SELECT
            
            COALESCE(SUM(pick_up_charges),0)
                AS totalPickUpCharges,
            
            COALESCE(SUM(deliver_charges),0)
                AS totalDeliveryCharges,
            
            COALESCE(SUM(tips),0)
                AS totalTips,
            
            COALESCE(SUM(surge_fee),0)
                AS totalSurgeFee,
            
            COALESCE(
                SUM(
                    COALESCE(pick_up_charges,0)
                    +
                    COALESCE(deliver_charges,0)
                    +
                    COALESCE(tips,0)
                    +
                    COALESCE(surge_fee,0)
                ),
                0
            ) AS totalEarnings,
            
            COUNT(driver_order_id)
                AS completedOrders
            
            FROM jippy_driver.driver_orders
            
            WHERE driver_id = :driverId
            """, nativeQuery = true)
    DriverTotalEarningsProjection fetchTotalEarnings(@Param("driverId") Integer driverId);

    @Query(value = """
            SELECT *
            FROM jippy_driver.driver_orders
            WHERE driver_id = :driverId
            AND order_id = :orderId
            """, nativeQuery = true)
    Optional<DriverOrder> findByDriverIdOrderId(@Param("driverId") Integer driverId, @Param("orderId") String orderId);


    // this query is used to fetch the settlement summary for drivers within a specified date range.
// It calculates the total number of orders completed and the total earnings for
// each driver based on the total delivery fees from the driver_orders table.
// The results are grouped by driver_id and ordered by driver_id for easy reference.
    @Query(value = """
            SELECT
                driver_id AS driverId,
                COUNT(order_id) AS noOfOrdersCompleted,
                SUM(total_delivery_fee) AS totalDriverEarnings
            FROM jippy_driver.driver_orders
            WHERE DATE(created_at)
            BETWEEN :startDate AND :endDate
            GROUP BY driver_id
            ORDER BY driver_id
            """, nativeQuery = true)
    List<DriverSettlementProjection> getDriversSettlementCalculation(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


//        this query is used to fetch the settlement details
//        for each order completed by drivers within a specified date range.
//        It retrieves the driver_id, order_id, pick_up_charges, deliver_charges,
//        total_delivery_fee, surge_fee, and tips for each order from the driver_orders table.

    @Query(value = """
            SELECT
                driver_id AS driverId,
                order_id AS orderId,
                pick_up_charges AS pickUpCharges,
                deliver_charges AS deliverCharges,
                total_delivery_fee AS totalDeliveryFee,
                surge_fee AS surgeFee,
                tips AS tips
            FROM jippy_driver.driver_orders
            WHERE DATE(created_at)
            BETWEEN :startDate AND :endDate
            ORDER BY driver_id
            """, nativeQuery = true)
    List<DriverOrderSettlementProjection> getDriverOrderSettlements
            (@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}


