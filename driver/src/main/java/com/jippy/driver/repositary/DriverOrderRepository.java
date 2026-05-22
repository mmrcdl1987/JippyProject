    package com.jippy.driver.repositary;

    import com.jippy.driver.entity.DriverOrder;
    import com.jippy.driver.projection.DriverOrderHistoryProjection;
    import com.jippy.driver.projection.DriverTotalEarningsProjection;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;

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
    d.tips AS tips

FROM jippy_driver.driver_orders d

WHERE d.driver_id = :driverId
""", nativeQuery = true)
List<DriverOrderHistoryProjection> fetchOrderEarningsHistory(
        @Param("driverId") Integer driverId);

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
        DriverTotalEarningsProjection fetchTotalEarnings(
                @Param("driverId") Integer driverId);

        @Query(value = """
                SELECT *
                FROM jippy_driver.driver_orders
                WHERE driver_id = :driverId
                AND order_id = :orderId
                """, nativeQuery = true)
        Optional<DriverOrder> findByDriverIdOrderId(
                @Param("driverId") Integer driverId,
                @Param("orderId") String orderId);
    }