package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.projection.CoDriverEarningsProjection;
import com.jippy.customerandorder.projection.CoOrderSettlementProjection;
import com.jippy.customerandorder.projection.CoOrderSettlementProjection;
import com.jippy.customerandorder.projection.CoSalesReportProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    CoDriverEarningsProjection fetchDriverEarnings(@Param("driverId") Integer driverId, @Param("date") LocalDate date);

    //
// to fetch Frequent outlets (>=3)
//    for ex: if a customer has ordered from outlet  5 times, outlet B 2 times,
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


    /*
     * REMINDER ORDERS
     */
    List<CoOrder> findByOrderTypeInAndScheduledDeliveryDateTimeBetween(
            List<String> orderTypes,
            LocalDateTime start,
            LocalDateTime end);





// Fetch delivered orders between dates and calculate total merchant settlement amount
//this fetches all orders with status 'DELIVERED' that were created between the specified start and end dates. It joins the orders table with the order_items table to calculate the total price for each order by summing up the merchant_price_total from the order_items. The results are grouped by order_id, outlet_id, order_status, and created_at, and ordered by created_at in descending order. The query returns a list of CoOrderSettlementProjection, which includes the order ID, outlet ID, order status, creation timestamp,
// and total price for each delivered order within the specified date range.
// ex: if there are 3 orders with status 'DELIVERED' created between the given dates,
// The query will return a list of 3 CoOrderSettlementProjection objects,
// each containing the order ID, outlet ID,
//    order status, creation timestamp, and total price for those orders.
@Query(value = """
            SELECT
                o.order_id AS orderId,
                o.outlet_id AS outletId,
                o.order_status AS orderStatus,
                o.created_at AS createdAt,
                SUM(oi.merchant_price_total) AS totalPrice
            FROM jippy_customer_and_order.orders o
            JOIN jippy_customer_and_order.order_items oi
            ON o.order_id = oi.order_id
            WHERE o.order_status = 'DELIVERED'
            AND DATE(o.created_at)
            BETWEEN :startDate AND :endDate
            GROUP BY
                o.order_id,
                o.outlet_id,
                o.order_status,
                o.created_at
            ORDER BY o.created_at DESC
            """, nativeQuery = true)
List<CoOrderSettlementProjection> getProductDetailsForMerchantSettlement
(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
Optional<CoOrder> findByOrderIdAndDriverId(
        String orderId,
        Long driverId);

//    @Query(value = """
//        SELECT
//            order_summary.sales_date AS salesDate,
//            COUNT(order_summary.order_id) AS totalOrders,
//            COALESCE(SUM(order_summary.order_earning), 0) AS totalEarnings
//        FROM
//        (
//            SELECT
//                o.order_id,
//                CAST(o.created_at AS DATE) AS sales_date,
//                SUM(oi.merchant_price_total) AS order_earning
//            FROM jippy_customer_and_order.orders o
//            INNER JOIN jippy_customer_and_order.order_items oi
//                    ON oi.order_id = o.order_id
//            WHERE o.outlet_id IN (:outletIds)
//              AND o.order_status = 'DELIVERED'
//              AND (
//                    :fromDate IS NULL
//                    OR CAST(o.created_at AS DATE)
//                       BETWEEN :fromDate AND :toDate
//                  )
//            GROUP BY
//                o.order_id,
//                CAST(o.created_at AS DATE)
//        ) order_summary
//        GROUP BY order_summary.sales_date
//        ORDER BY order_summary.sales_date DESC
//        """,
//            nativeQuery = true)
//    List<CoSalesReportProjection> getSalesReport(
//            @Param("outletIds") List<Integer> outletIds,
//            @Param("fromDate") LocalDate fromDate,
//            @Param("toDate") LocalDate toDate);
//
//    @Query(value = """
//        SELECT
//            order_summary.sales_date AS salesDate,
//            COUNT(order_summary.order_id) AS totalOrders,
//            COALESCE(SUM(order_summary.order_earning), 0) AS totalEarnings
//        FROM
//        (
//            SELECT
//                o.order_id,
//                CAST(o.created_at AS DATE) AS sales_date,
//                SUM(oi.merchant_price_total) AS order_earning
//            FROM jippy_customer_and_order.orders o
//            INNER JOIN jippy_customer_and_order.order_items oi
//                    ON oi.order_id = o.order_id
//            WHERE o.outlet_id = :outletId
//              AND o.order_status = 'DELIVERED'
//              AND (
//                    :fromDate IS NULL
//                    OR CAST(o.created_at AS DATE)
//                       BETWEEN :fromDate AND :toDate
//                  )
//            GROUP BY
//                o.order_id,
//                CAST(o.created_at AS DATE)
//        ) order_summary
//        GROUP BY order_summary.sales_date
//        ORDER BY order_summary.sales_date DESC
//        """,
//            nativeQuery = true)
//    List<CoSalesReportProjection> getSalesReportByOutlet(
//            @Param("outletId") Integer outletId,
//            @Param("fromDate") LocalDate fromDate,
//            @Param("toDate") LocalDate toDate);

    @Query(value = """
        SELECT
            order_summary.sales_date AS salesDate,
            COUNT(order_summary.order_id) AS totalOrders,
            COALESCE(SUM(order_summary.order_earning),0) AS totalEarnings
        FROM
        (
            SELECT
                o.order_id,
                CAST(o.created_at AS DATE) AS sales_date,
                SUM(oi.merchant_price_total) AS order_earning
            FROM jippy_customer_and_order.orders o
            INNER JOIN jippy_customer_and_order.order_items oi
                    ON oi.order_id = o.order_id
            WHERE o.outlet_id IN (:outletIds)
              AND o.order_status = 'DELIVERED'
            GROUP BY
                o.order_id,
                CAST(o.created_at AS DATE)
        ) order_summary
        GROUP BY order_summary.sales_date
        ORDER BY order_summary.sales_date DESC
        """,
            nativeQuery = true)
    List<CoSalesReportProjection> getSalesReport(
            @Param("outletIds") List<Integer> outletIds);

    @Query(value = """
        SELECT
            order_summary.sales_date AS salesDate,
            COUNT(order_summary.order_id) AS totalOrders,
            COALESCE(SUM(order_summary.order_earning),0) AS totalEarnings
        FROM
        (
            SELECT
                o.order_id,
                CAST(o.created_at AS DATE) AS sales_date,
                SUM(oi.merchant_price_total) AS order_earning
            FROM jippy_customer_and_order.orders o
            INNER JOIN jippy_customer_and_order.order_items oi
                    ON oi.order_id = o.order_id
            WHERE o.outlet_id IN (:outletIds)
              AND o.order_status = 'DELIVERED'
              AND CAST(o.created_at AS DATE)
                  BETWEEN :fromDate AND :toDate
            GROUP BY
                o.order_id,
                CAST(o.created_at AS DATE)
        ) order_summary
        GROUP BY order_summary.sales_date
        ORDER BY order_summary.sales_date DESC
        """,
            nativeQuery = true)
    List<CoSalesReportProjection> getSalesReportByDateRange(
            @Param("outletIds") List<Integer> outletIds,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query(value = """
        SELECT
            order_summary.sales_date AS salesDate,
            COUNT(order_summary.order_id) AS totalOrders,
            COALESCE(SUM(order_summary.order_earning),0) AS totalEarnings
        FROM
        (
            SELECT
                o.order_id,
                CAST(o.created_at AS DATE) AS sales_date,
                SUM(oi.merchant_price_total) AS order_earning
            FROM jippy_customer_and_order.orders o
            INNER JOIN jippy_customer_and_order.order_items oi
                    ON oi.order_id = o.order_id
            WHERE o.outlet_id = :outletId
              AND o.order_status = 'DELIVERED'
            GROUP BY
                o.order_id,
                CAST(o.created_at AS DATE)
        ) order_summary
        GROUP BY order_summary.sales_date
        ORDER BY order_summary.sales_date DESC
        """,
            nativeQuery = true)
    List<CoSalesReportProjection> getSalesReportByOutlet(
            @Param("outletId") Integer outletId);

    @Query(value = """
        SELECT
            order_summary.sales_date AS salesDate,
            COUNT(order_summary.order_id) AS totalOrders,
            COALESCE(SUM(order_summary.order_earning),0) AS totalEarnings
        FROM
        (
            SELECT
                o.order_id,
                CAST(o.created_at AS DATE) AS sales_date,
                SUM(oi.merchant_price_total) AS order_earning
            FROM jippy_customer_and_order.orders o
            INNER JOIN jippy_customer_and_order.order_items oi
                    ON oi.order_id = o.order_id
            WHERE o.outlet_id = :outletId
              AND o.order_status = 'DELIVERED'
              AND CAST(o.created_at AS DATE)
                  BETWEEN :fromDate AND :toDate
            GROUP BY
                o.order_id,
                CAST(o.created_at AS DATE)
        ) order_summary
        GROUP BY order_summary.sales_date
        ORDER BY order_summary.sales_date DESC
        """,
            nativeQuery = true)
    List<CoSalesReportProjection> getSalesReportByOutletAndDateRange(
            @Param("outletId") Integer outletId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);


    Optional<CoOrder> findByOrderIdAndCustomerId(String orderId, Integer customerId);
}
