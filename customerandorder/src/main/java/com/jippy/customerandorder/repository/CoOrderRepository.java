package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.projection.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = """
        SELECT
            COALESCE(
                SUM(
                    COALESCE(opb.driver_delivery_fee, 0)
                    + COALESCE(opb.surge_fee, 0)
                    + COALESCE(opb.tip, 0)
                ),
                0
            ) AS totalEarnings,

            COUNT(DISTINCT o.order_id) AS ordersCount

        FROM jippy_customer_and_order.orders o

        LEFT JOIN jippy_customer_and_order.order_price_breakup opb
            ON opb.order_id = o.order_id

        WHERE o.driver_id = :driverId
          AND DATE(o.created_at) = :date
        """, nativeQuery = true)
    CoDriverEarningsProjection fetchDriverEarnings(
            @Param("driverId") Integer driverId,
            @Param("date") LocalDate date
    );
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
    List<CoOrder> findByOrderTypeInAndScheduledDeliveryDateTimeBetween(List<String> orderTypes, LocalDateTime start, LocalDateTime end);


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
    List<CoOrderSettlementProjection> getProductDetailsForMerchantSettlement(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    Optional<CoOrder> findByOrderIdAndDriverId(String orderId, Long driverId);

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
            """, nativeQuery = true)
    List<CoSalesReportProjection> getSalesReport(@Param("outletIds") List<Integer> outletIds);

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
            """, nativeQuery = true)
    List<CoSalesReportProjection> getSalesReportByDateRange(@Param("outletIds") List<Integer> outletIds, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

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
            """, nativeQuery = true)
    List<CoSalesReportProjection> getSalesReportByOutlet(@Param("outletId") Integer outletId);

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
            """, nativeQuery = true)
    List<CoSalesReportProjection> getSalesReportByOutletAndDateRange(@Param("outletId") Integer outletId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);


    Optional<CoOrder> findByOrderIdAndCustomerId(String orderId, Integer customerId);

    List<CoOrder> findByGroupOrderInvitationIdAndCustomerId(Integer goInvitationId, Integer customerId);

    boolean existsByCustomerIdAndOrderStatus(Integer customerId, String orderStatus);

//    ===============================================================================
//    ===============================================================================

    /**
     * Fetches the main order details along with:
     * <p>
     * - Customer details
     * - Customer delivery building name
     * - Payment mode
     * - Outlet ID
     */
    @Query(value = """
            SELECT
            
                -- ================= ORDER =================
            
                o.order_id AS "orderId",
            
                o.created_at AS "createdAt",
            
                o.order_type AS "orderType",
            
                o.order_status AS "orderStatus",
            
                o.driver_id AS "driverId",
            
                -- ================= ORDER TIMELINE =================
            
                o.merchant_accepted_time AS "merchantAcceptedTime",
            
                o.food_preparation_completed_time AS "foodPreparationCompletedTime",
            
                o.driver_order_accepted_time AS "driverOrderAcceptedTime",
            
                o.driver_outlet_reached_time AS "driverOutletReachedTime",
            
                o.driver_food_pickup_time AS "driverFoodPickupTime",
            
                o.driver_food_delivered_time AS "driverFoodDeliveredTime",
            
                -- ================= CUSTOMER =================
            
                c.customer_id AS "customerId",
            
                CONCAT(
                    COALESCE(c.first_name, ''),
                    CASE
                        WHEN c.last_name IS NOT NULL
                             AND c.last_name <> ''
                        THEN CONCAT(' ', c.last_name)
                        ELSE ''
                    END
                ) AS "customerName",
            
                c.email AS "email",
            
                c.phone_number AS "phoneNumber",
            
                cda.building_name AS "buildingName",
            
                -- ================= OUTLET =================
            
                o.outlet_id AS "outletId",
            
                -- ================= PAYMENT =================
            
                o.payment_mode_id AS "paymentModeId",
            
                pm.payment_mode AS "paymentMode"
            
            FROM jippy_customer_and_order.orders o
            
            -- ================= CUSTOMER =================
            
            LEFT JOIN jippy_customer_and_order.customer c
                ON c.customer_id = o.customer_id
            
            -- ================= CUSTOMER ADDRESS =================
            
            LEFT JOIN jippy_customer_and_order.customer_delivery_addresses cda
                ON cda.customer_address_id =
                   o.customer_delivery_address_id
            
            -- ================= PAYMENT MODE =================
            
            LEFT JOIN jippy_customer_and_order.payment_modes pm
                ON pm.payment_mode_id = o.payment_mode_id
            
            WHERE o.order_id = :orderId
            """, nativeQuery = true)
    Optional<CoOrderCompleteDetailsProjection> getOrderCompleteDetails(@Param("orderId") String orderId);
//    ======================================================================================
//    ======================================================================================

    /**
     * Fetches order flow counts for multiple outlet IDs.
     * <p>
     * This is used for:
     * <p>
     * 1. Merchant-wise order count
     * 2. Outlet-wise order count
     * <p>
     * The query counts:
     * <p>
     * - Total orders
     * - ORDER_COMPLETED orders
     * - ORDER_REJECTED orders
     */
    @Query(value = """
            SELECT
            
                COUNT(*) AS "totalOrdersCount",
            
                COUNT(
                    CASE
                        WHEN o.order_status = 'ORDER_COMPLETED'
                        THEN 1
                    END
                ) AS "completedOrdersCount",
            
                COUNT(
                    CASE
                        WHEN o.order_status = 'ORDER_REJECTED'
                        THEN 1
                    END
                ) AS "rejectedOrdersCount"
            
            FROM "jippy_customer_and_order"."orders" o
            
            WHERE o.outlet_id IN (:outletIds)
            
            """, nativeQuery = true)
    CoOrderFlowCountProjection getOrderFlowCountsByOutletIds(@Param("outletIds") List<Integer> outletIds);

    /**
     * Fetches total, completed and rejected order counts
     * for a specific driver.
     *
     * <p>
     * Counts are calculated directly from the CO orders table
     * using driver_id.
     */
    @Query(value = """
            SELECT
                COUNT(*) AS totalOrdersCount,
            
                COUNT(
                    CASE
                        WHEN o.order_status = 'ORDER_COMPLETED'
                        THEN 1
                    END
                ) AS completedOrdersCount,
            
                COUNT(
                    CASE
                        WHEN o.order_status = 'ORDER_REJECTED'
                        THEN 1
                    END
                ) AS rejectedOrdersCount
            
            FROM "jippy_customer_and_order"."orders" o
            
            WHERE o.driver_id = :driverId
            """, nativeQuery = true)
    CoOrderFlowCountProjection getOrderFlowCountsByDriverId(@Param("driverId") Integer driverId);

    //    ==============================================================================
//    ==============================================================================
    @Query(value = """
            SELECT
                o.order_id AS orderId,
                o.outlet_id AS outletId,
                o.customer_id AS customerId,
            
                CONCAT_WS(
                    ' ',
                    c.first_name,
                    c.last_name
                ) AS customerName,
            
                o.driver_id AS driverId,
                o.order_status AS orderStatus,
            
                COALESCE(
                    SUM(oi.merchant_total_price),
                    0
                ) AS merchantTotalPrice
            
            FROM jippy_customer_and_order.orders o
            
            LEFT JOIN jippy_customer_and_order.customer c
                ON c.customer_id = o.customer_id
            
            LEFT JOIN jippy_customer_and_order.order_items oi
                ON oi.order_id = o.order_id
            
            WHERE o.outlet_id = :outletId
            
            GROUP BY
                o.order_id,
                o.outlet_id,
                o.customer_id,
                c.first_name,
                c.last_name,
                o.driver_id,
                o.order_status,
                o.created_at
            
            ORDER BY o.created_at DESC
            """, countQuery = """
            SELECT COUNT(*)
            FROM jippy_customer_and_order.orders o
            WHERE o.outlet_id = :outletId
            """, nativeQuery = true)
    Page<CoOrderDetailsOfOutletProjection> getOrderDetailsOfOutlet(@Param("outletId") Integer outletId, Pageable pageable);
//    ======================================================================================
//    ======================================================================================

    @Query(value = """
            SELECT
                o.order_id AS orderId,
                o.outlet_id AS outletId,
            
                CONCAT_WS(
                    ' ',
                    c.first_name,
                    c.last_name
                ) AS customerName,
            
                o.driver_id AS driverId,
                o.order_status AS orderStatus,
            
                opb.pick_up_distance_in_kms AS pickUpDistanceInKms,
                opb.delivery_distance_in_kms AS deliveryDistanceInKms,
                opb.pick_up_charges AS pickUpCharges,
                opb.driver_delivery_fee AS driverDeliveryFee,
            
                COALESCE(opb.pick_up_charges, 0)
                    + COALESCE(opb.driver_delivery_fee, 0)
                    AS driverTotalCharges
            
            FROM jippy_customer_and_order.orders o
            
            LEFT JOIN jippy_customer_and_order.customer c
                ON c.customer_id = o.customer_id
            
            LEFT JOIN jippy_customer_and_order.order_price_breakup opb
                ON opb.order_id = o.order_id
            
            WHERE o.driver_id = :driverId
            
            ORDER BY o.created_at DESC
            """,

            countQuery = """
                    SELECT COUNT(*)
                    FROM jippy_customer_and_order.orders o
                    WHERE o.driver_id = :driverId
                    """,

                    nativeQuery = true
            )
            Page<CoOrderDetailsOfDriverProjection> getOrderDetailsOfDriver(
                    @Param("driverId") Integer driverId,
                    Pageable pageable
            );
// =========================================================================================
    @Query(value = """
                    SELECT order_item_id,oi.order_id,oi.product_id,oi.variant_option_id,oi.quantity,oi.merchant_unit_price,oi.merchant_total_price,
                    o.order_status,o.cooking_instructions,o.is_cutlery_required,o.created_at 
                    FROM "jippy_customer_and_order"."order_items" oi
                    left join "jippy_customer_and_order"."orders"  o on o.order_id = oi.order_id where o.outlet_id =:outletId
                    order by o.created_at desc """,
                    countQuery = """
                    SELECT COUNT(*) FROM "jippy_customer_and_order"."orders" o WHERE o.outlet_id = :outletId
                    """,
            nativeQuery = true)
    List<OrderSummaryProjection> findAllOrdersByOutletId(@Param("outletId") Integer outletId, Pageable pageable);


    @Query(value = """
                    SELECT o.order_id,c.first_name,c.phone_number,cda.building_name as customer_address,ST_X(location::geometry) AS customer_longitude,
                        ST_Y(location::geometry) AS customer_latitude,oi.product_id,oi.variant_option_id,oi.quantity,opb.order_total_amount,o.outlet_id
                        FROM "jippy_customer_and_order"."orders" o
                    join "jippy_customer_and_order"."order_items" oi on o.order_id = oi.order_id
                    join "jippy_customer_and_order"."order_price_breakup" opb on opb.order_id = o.order_id
                    join "jippy_customer_and_order"."customer" c on c.customer_id = o.customer_id
                    join "jippy_customer_and_order"."customer_delivery_addresses" cda on cda.customer_address_id = o.customer_delivery_address_id\s
                    where o.order_id = :orderId
                    """,nativeQuery = true)
    List<CoOrderDetailsForDeliveryProjection> getOrderDetailsForDelivery(@Param("orderId") String orderId);
    // =========================================================================================
    @Query(value = """
            SELECT
            
                (
                    SELECT COALESCE(SUM(oi.merchant_total_price), 0)
                    FROM jippy_customer_and_order.orders o1
                    JOIN jippy_customer_and_order.order_items oi
                        ON oi.order_id = o1.order_id
                    WHERE o1.outlet_id = :outletId
                      AND o1.order_status = 'ORDER_DELIVERED'
                ) AS merchantTotalPrice,
            
                (
                    SELECT COALESCE(SUM(opb.discount), 0)
                    FROM jippy_customer_and_order.orders o2
                    JOIN jippy_customer_and_order.order_price_breakup opb
                        ON opb.order_id = o2.order_id
                    WHERE o2.outlet_id = :outletId
                      AND o2.order_status = 'ORDER_DELIVERED'
                      AND opb.discount_type = 'MERCHANT_PROMOTION'
                ) AS promotionDiscount
            
            """, nativeQuery = true)
    CoMerchantSettlementProjection getMerchantSettlementDetails(@Param("outletId") Integer outletId);
//        ===================================================================================================
//        ===================================================================================================

    /**
     * Fetches merchant settlement calculation details for the given outlets
     * within the specified date range.
     *
     * Conditions:
     * - Order must belong to the given outlet IDs.
     * - Order status must be ORDER_DELIVERED.
     * - Order created_at must be between startDate and endDate.
     * - merchant_total_price is summed from order_items.
     * - MERCHANT_PROMOTION discount is summed from order_price_breakup.
     *
     * order_items and order_price_breakup are aggregated separately
     * to avoid duplicate amounts caused by joining multiple child rows.
     */
    @Query(value = """
        SELECT
            o.outlet_id AS outletId,
        
            COUNT(DISTINCT o.order_id) AS orderCount,
        
            COALESCE(SUM(oi.merchantTotalAmount), 0) AS merchantTotalAmount,
        
            COALESCE(SUM(opb.promotionDeductedAmount), 0) AS promotionDeductedAmount
        
        FROM jippy_customer_and_order.orders o
        
        LEFT JOIN (
            SELECT
                order_id,
                SUM(merchant_total_price) AS merchantTotalAmount
            FROM jippy_customer_and_order.order_items
            GROUP BY order_id
        ) oi
            ON oi.order_id = o.order_id
        
        LEFT JOIN (
            SELECT
                order_id,
                SUM(
                    CASE
                        WHEN UPPER(discount_type) = 'MERCHANT_PROMOTION'
                        THEN discount
                        ELSE 0
                    END
                ) AS promotionDeductedAmount
            FROM jippy_customer_and_order.order_price_breakup
            GROUP BY order_id
        ) opb
            ON opb.order_id = o.order_id
        
        WHERE o.outlet_id IN (:outletIds)
        
          AND o.created_at >= :startDate
        
          AND o.created_at < CAST(:endDate AS DATE) + INTERVAL '1 day'
        
          AND UPPER(o.order_status) = 'ORDER_DELIVERED'
        
        GROUP BY o.outlet_id
        """, nativeQuery = true)
    List<CoMerchantSettlementForOutletProjection> findMerchantSettlementForOutlets(
            @Param("outletIds") List<Integer> outletIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


//      ==================================================================================
//      ==================================================================================
    /**
     * Fetches outlet and order settlement details
     * for the selected settlement period.
     *
     * The query returns:
     *
     * 1. Number of unique outlets having delivered orders.
     *
     * 2. Number of delivered orders.
     *
     * 3. Total merchant amount from order_items.merchant_total_price
     *    for all delivered orders.
     *
     * 4. Total merchant promotion discount from
     *    order_price_breakup.discount where discount_type is
     *    MERCHANT_PROMOTION.
     *
     * Only orders with ORDER_DELIVERED status are considered.
     *
     * The settlement period includes the complete end date.
     */
    @Query(value = """
        SELECT

            COUNT(DISTINCT o.outlet_id)
                AS "outletsCountsBetweenDates",

            COUNT(DISTINCT o.order_id)
                AS "orderCountsBetweenDates",

            COALESCE(
                (
                    SELECT SUM(oi.merchant_total_price)
                    FROM jippy_customer_and_order.order_items oi
                    WHERE oi.order_id IN (
                        SELECT od.order_id
                        FROM jippy_customer_and_order.orders od
                        WHERE od.order_status = 'ORDER_DELIVERED'
                          AND od.created_at >= :startDate
                          AND od.created_at < CAST(:endDate AS DATE) + INTERVAL '1 day'
                    )
                ),
                0
            )
                AS "merchantTotalPriceBetweenDates",

            COALESCE(
                (
                    SELECT SUM(opb.discount)
                    FROM jippy_customer_and_order.order_price_breakup opb
                    WHERE opb.discount_type = 'MERCHANT_PROMOTION'
                      AND opb.order_id IN (
                          SELECT od.order_id
                          FROM jippy_customer_and_order.orders od
                          WHERE od.order_status = 'ORDER_DELIVERED'
                            AND od.created_at >= :startDate
                            AND od.created_at < CAST(:endDate AS DATE) + INTERVAL '1 day'
                      )
                ),
                0
            )
                AS "promotionDeductedAmount"

        FROM jippy_customer_and_order.orders o

        WHERE o.order_status = 'ORDER_DELIVERED'
          AND o.created_at >= :startDate
          AND o.created_at < CAST(:endDate AS DATE) + INTERVAL '1 day'

        """, nativeQuery = true)
    CoOutletAndOrdersSettlementProjection getOutletAndOrdersSettlementDetails(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
//    ===================================================================================
//    ===================================================================================
    /**
     * Fetches the unique outlet IDs that have delivered orders
     * within the selected settlement period.
     *
     * Only delivered orders are considered because settlement
     * calculations are based on delivered orders.
     */
    @Query(value = """
        SELECT DISTINCT o.outlet_id
        FROM jippy_customer_and_order.orders o
        WHERE o.order_status = 'ORDER_DELIVERED'
          AND o.created_at >= :startDate
          AND o.created_at < CAST(:endDate AS DATE) + INTERVAL '1 day'
        ORDER BY o.outlet_id
        """, nativeQuery = true)
    List<Integer> findDeliveredOrderOutletIdsBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    //    =======================================================================================
//    =======================================================================================
    @Query(value = """
                SELECT
                    o.outlet_id AS "outletId",
        
                    COALESCE(SUM(oi.merchant_total_price), 0)
                        AS "merchantTotalPrice",
        
                    COALESCE(
                        (
                            SELECT SUM(opb.discount)
                            FROM jippy_customer_and_order.order_price_breakup opb
                            WHERE opb.discount_type = 'MERCHANT_PROMOTION'
                              AND opb.order_id IN (
                                  SELECT od.order_id
                                  FROM jippy_customer_and_order.orders od
                                  WHERE od.outlet_id = o.outlet_id
                                    AND od.order_status = 'ORDER_DELIVERED'
                                    AND od.created_at >= :startDate
                                    AND od.created_at < CAST(:endDate AS DATE) + INTERVAL '1 day'
                              )
                        ),
                        0
                    ) AS "promotionDeductedAmount"
        
                FROM jippy_customer_and_order.orders o
        
                JOIN jippy_customer_and_order.order_items oi
                    ON oi.order_id = o.order_id
        
                WHERE o.order_status = 'ORDER_DELIVERED'
                  AND o.created_at >= :startDate
                  AND o.created_at < CAST(:endDate AS DATE) + INTERVAL '1 day'
        
                GROUP BY o.outlet_id
        
                ORDER BY o.outlet_id
                """, nativeQuery = true)
    List<CoOutletSettlementProjection> getOutletSettlementDetailsBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    //        =================================================================================
//        =================================================================================
    @Query(value = """
 SELECT
        o.outlet_id AS "outletId",

        COUNT(DISTINCT o.order_id) AS "orderCount",

        COALESCE(SUM(oi.merchant_total_amount), 0) AS "merchantTotalAmount",

        COALESCE(SUM(opb.promotion_amount), 0) AS "promotionDeductedAmount"

    FROM jippy_customer_and_order.orders o

    LEFT JOIN (
        SELECT
            order_id,
            SUM(merchant_total_price) AS merchant_total_amount
        FROM jippy_customer_and_order.order_items
        GROUP BY order_id
    ) oi
        ON oi.order_id = o.order_id

    LEFT JOIN (
        SELECT
            order_id,
            SUM(discount) AS promotion_amount
        FROM jippy_customer_and_order.order_price_breakup
        WHERE discount_type = 'MERCHANT_PROMOTION'
        GROUP BY order_id
    ) opb
        ON opb.order_id = o.order_id

    WHERE o.outlet_id = :outletId
      AND o.order_status = 'ORDER_DELIVERED'
      AND o.created_at >= :startDate
      AND o.created_at < :endDate

    GROUP BY o.outlet_id
        """, nativeQuery = true)
    Optional<CoMerchantSettlementForOutletProjection> findMerchantSettlementForOutlet(
            @Param("outletId") Integer outletId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
//    =================================================================================
    /**
     * Fetches only delivered orders for merchant settlement.
     *
     * Settlement is calculated only for orders whose status is
     * ORDER_DELIVERED.
     */
    @Query(value = """
    SELECT
        o.order_id AS "orderId",
        o.created_at AS "createdAt",

        oi.product_id AS "productId",
        oi.variant_option_id AS "variantOptionId",
        oi.quantity AS "quantity",
        oi.merchant_total_price AS "merchantTotalPrice",

        COALESCE(
            (
                SELECT SUM(opb.discount)
                FROM jippy_customer_and_order.order_price_breakup opb
                WHERE opb.order_id = o.order_id
                  AND opb.discount_type = 'MERCHANT_PROMOTION'
            ),
            0
        ) AS "promotionDeductedAmount",

        'MERCHANT_PROMOTION' AS "discountType"

    FROM jippy_customer_and_order.orders o

    INNER JOIN jippy_customer_and_order.order_items oi
        ON oi.order_id = o.order_id

    WHERE o.outlet_id = :outletId
      AND o.order_status = 'ORDER_DELIVERED'
      AND o.created_at >= :startDate
      AND o.created_at < :endDate

    ORDER BY
        o.created_at ASC,
        o.order_id ASC,
        oi.order_item_id ASC
    """,
            nativeQuery = true)
    List<CoMerchantSettlementOrderDetailsProjection> getMerchantSettlementOrderDetails(
            @Param("outletId") Integer outletId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    /**
     * Fetches order-item details required for merchant settlement.
     *
     * Tables used:
     *
     * 1. orders
     * 2. order_items
     * 3. order_price_breakup
     *
     * The order is filtered using:
     *
     * - outletId
     * - orders.created_at
     * - requested settlement date range
     *
     * The query returns:
     *
     * - orderId
     * - createdAt
     * - productId
     * - variantOptionId
     * - quantity
     * - merchantTotalPrice
     * - promotionDeductedAmount
     * - discountType
     *
     * FM-related information is intentionally not fetched here.
     * FM will use productId and variantOptionId to fetch:
     *
     * - productName
     * - variantName
     * - GST information
     */
}
