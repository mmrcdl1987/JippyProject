package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoDriverOrder;
import com.jippy.customerandorder.projection.CoDriverOrderHistoryProjection;
import com.jippy.customerandorder.projection.CoDriverTotalEarningsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoDriverOrderRepository extends JpaRepository<CoDriverOrder, Integer> {

    //    this feildsMethods(getters) should match projection interface CoDriverOrderHistoryProjection
    @Query(value = """
            SELECT
            
                d.driver_id AS driverId,
            
                d.order_id AS orderId,
            
                d.pick_up_distance_in_kms
                    AS pickUpDistanceInKms,
            
                d.delivery_distance_in_kms
                    AS deliveryDistanceInKms,
            
                d.pick_up_charges
                    AS pickUpCharges,
            
                d.deliver_charges
                    AS deliverCharges,
            
                d.total_delivery_fee
                    AS totalDeliveryFee,
            
                d.surge_fee
                    AS surgeFee,
            
                d.tips
                    AS tips,
            
                -- from orders table
                o.order_status
                    AS orderStatus,
            
                -- internal use only
                o.outlet_id
                    AS outletId
            
            FROM jippy_customer_and_order.driver_orders d
            
            JOIN jippy_customer_and_order.orders o
                 --ON CAST(d.order_id AS VARCHAR) = o.order_id
                 on d.order_id = o.order_id
            
            WHERE d.driver_id = :driverId
            """, nativeQuery = true)
    List<CoDriverOrderHistoryProjection> fetchOrderEarningsHistory(@Param("driverId") Integer driverId);


    @Query(value = """
            SELECT
            
            COALESCE(
                SUM(pick_up_charges),
                0
            ) AS totalPickUpCharges,
            
            COALESCE(
                SUM(deliver_charges),
                0
            ) AS totalDeliveryCharges,
            
            COALESCE(
                SUM(tips),
                0
            ) AS totalTips,
            
            COALESCE(
                SUM(surge_fee),
                0
            ) AS totalSurgeFee,
            
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
            
            FROM jippy_customer_and_order.driver_orders
            
            WHERE driver_id = :driverId
            """, nativeQuery = true)
    CoDriverTotalEarningsProjection fetchTotalEarnings(@Param("driverId") Integer driverId);
}