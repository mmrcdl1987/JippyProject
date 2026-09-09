

package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.projection.CoCompleteOrdersFlowCountsProjection;
import com.jippy.customerandorder.projection.CoOrderDetailsByOrderStatusProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CoCustomerRepository extends JpaRepository<CoCustomer, Integer> {

    Optional<CoCustomer> findByEmail(String email);

    Optional<CoCustomer> findByPhoneNumber(String phoneNumber);

    Optional<CoCustomer> findByReferralCode(String referralCode);

    boolean existsByPhoneNumberAndCustomerId(String phoneNumber, Integer customerId);

    boolean existsByPhoneNumberAndCustomerIdNot(String phoneNumber, Integer customerId);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query(value = """
            SELECT *
            FROM jippy_customer_and_order.customer
            WHERE CURRENT_DATE - DATE(created_at)
            IN (15,30,60)
            """, nativeQuery = true)
    List<CoCustomer> findEligibleWelcomeCustomers();

    @Query(value = """
            SELECT c.*
            FROM jippy_customer_and_order.customer c
            INNER JOIN jippy_customer_and_order.customer_status cs
                    ON c.customer_status_id = cs.customer_status_id
            WHERE cs.status_name = 'NEW'
              AND c.created_at <= NOW() - INTERVAL '24 HOURS'
            ORDER BY c.created_at ASC
            """, nativeQuery = true)
    List<CoCustomer> findProfileIncompleteCustomers();

    @Query(value = """
        SELECT *
        FROM jippy_customer_and_order.customer
        ORDER BY customer_id
        """, nativeQuery = true)
    List<CoCustomer> findAllCustomersForMealReminder();

    Optional<CoCustomer> findByCustomerId(Integer customerId);

    /**
     * Fetches complete order flow counts based on order_status.
     *
     * Total orders      -> Counts all orders.
     * Orders placed     -> ORDER_PLACED
     * Orders confirmed  -> ORDER_CONFIRMED
     * Orders shipped    -> ORDER_SHIPPED
     * Orders completed  -> ORDER_COMPLETED
     */
    @Query(value = """
        SELECT
            COUNT(*) AS totalOrdersCount,

            COUNT(
                CASE
                    WHEN order_status = 'ORDER_PLACED'
                    THEN 1
                END
            ) AS ordersPlaced,

            COUNT(
                CASE
                    WHEN order_status = 'ORDER_CONFIRMED'
                    THEN 1
                END
            ) AS ordersConfirmed,

            COUNT(
                CASE
                    WHEN order_status = 'ORDER_SHIPPED'
                    THEN 1
                END
            ) AS ordersShipped,

            COUNT(
                CASE
                    WHEN order_status = 'ORDER_COMPLETED'
                    THEN 1
                END
            ) AS ordersCompleted,

            COUNT(
                CASE
                    WHEN order_status = 'ORDER_REJECTED'
                    THEN 1
                END
            ) AS ordersRejected

        FROM "jippy_customer_and_order"."orders"
        """,
            nativeQuery = true)
    CoCompleteOrdersFlowCountsProjection getCompleteOrdersFlowCounts();
//    =================================================================================
//    =================================================================================

    /**
     * Fetches order details based on the requested order status.
     *
     * The following fields are returned:
     *
     * - order_id
     * - outlet_id
     * - driver_id
     * - order_status
     *
     * Only orders matching the supplied order status are returned.
     *
     * Example:
     * ORDER_SHIPPED
     */

    /**
     * Fetches order details from CO database based on order status.
     *
     * Customer name is fetched from customer table.
     * Order amount is fetched from order_price_breakup table.
     *
     * FM and Driver information are intentionally not fetched here
     * because they belong to separate microservices.
     */
    @Query(
            value = """
                SELECT
                    o.order_id AS orderId,
                    o.outlet_id AS outletId,
                    o.driver_id AS driverId,
                    o.order_status AS orderStatus,

                    CONCAT_WS(
                        ' ',
                        c.first_name,
                        c.last_name
                    ) AS customerName,

                    opb.order_total_amount AS orderAmount

                FROM "jippy_customer_and_order"."orders" o

                LEFT JOIN "jippy_customer_and_order"."customer" c
                    ON c.customer_id = o.customer_id

                LEFT JOIN "jippy_customer_and_order"."order_price_breakup" opb
                    ON opb.order_id = o.order_id

                WHERE o.order_status = :orderStatus

                ORDER BY o.created_at DESC
                """,

            countQuery = """
                SELECT COUNT(*)
                FROM "jippy_customer_and_order"."orders" o

                WHERE o.order_status = :orderStatus
                """,

            nativeQuery = true
    )
    Page<CoOrderDetailsByOrderStatusProjection>
    getCompleteOrdersDetailsByOrderStatus(
            @Param("orderStatus") String orderStatus,
            Pageable pageable
    );

//    =====================================================================================
}