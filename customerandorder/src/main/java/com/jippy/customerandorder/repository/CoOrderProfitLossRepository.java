package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.projection.CoOrderProfitLossProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CoOrderProfitLossRepository extends JpaRepository<CoOrder, String> {

    /**
     * Get profit/loss input data for a single order.
     */
    @Query(value = """
        SELECT
            o.order_id AS orderId,
            o.outlet_id AS outletId,
            o.order_status AS orderStatus,

            -- Amount paid by customer to Jippy
            COALESCE(opb.order_total_amount, 0) AS revenue,

            -- Total merchant amount before GST/merchant promotion
            COALESCE(SUM(oi.merchant_total_price), 0) AS merchantTotalPrice,

            -- Delivery fee paid by Jippy
            COALESCE(opb.total_delivery_fee, 0) AS driverDeliveryFee,

            -- Discount amount
            COALESCE(opb.discount, 0) AS discountAmount,

            -- Discount type
            opb.discount_type AS discountType

        FROM jippy_customer_and_order.orders o

        LEFT JOIN jippy_customer_and_order.order_price_breakup opb
            ON opb.order_id = o.order_id

        LEFT JOIN jippy_customer_and_order.order_items oi
            ON oi.order_id = o.order_id

        WHERE o.order_id = :orderId

        GROUP BY
            o.order_id,
            o.outlet_id,
            o.order_status,
            opb.order_total_amount,
            opb.total_delivery_fee,
            opb.discount,
            opb.discount_type
        """, nativeQuery = true)
    Optional<CoOrderProfitLossProjection> getOrderProfitLossData(
            @Param("orderId") String orderId
    );


    /**
     * Get profit/loss input data for all orders with optional filters.
     */
    @Query(value = """
    SELECT
        o.order_id AS orderId,
        o.outlet_id AS outletId,
        o.order_status AS orderStatus,

        COALESCE(opb.order_total_amount, 0) AS revenue,

        COALESCE(SUM(oi.merchant_total_price), 0) AS merchantTotalPrice,

        COALESCE(opb.total_delivery_fee, 0) AS driverDeliveryFee,

        COALESCE(opb.discount, 0) AS discountAmount,

        opb.discount_type AS discountType

    FROM jippy_customer_and_order.orders o

    LEFT JOIN jippy_customer_and_order.order_price_breakup opb
        ON opb.order_id = o.order_id

    LEFT JOIN jippy_customer_and_order.order_items oi
        ON oi.order_id = o.order_id

    WHERE o.order_status = 'ORDER_COMPLETED'

        AND (
            CAST(:fromDate AS DATE) IS NULL
            OR DATE(o.created_at) >= CAST(:fromDate AS DATE)
        )

        AND (
            CAST(:toDate AS DATE) IS NULL
            OR DATE(o.created_at) <= CAST(:toDate AS DATE)
        )

    GROUP BY
        o.order_id,
        o.outlet_id,
        o.order_status,
        opb.order_total_amount,
        opb.total_delivery_fee,
        opb.discount,
        opb.discount_type,
        o.created_at

    ORDER BY o.created_at DESC
    """, nativeQuery = true)
    List<CoOrderProfitLossProjection> getAllOrdersProfitLossData(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}