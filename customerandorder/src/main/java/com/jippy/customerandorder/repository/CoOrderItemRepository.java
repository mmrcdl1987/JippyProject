package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrderItem;
import com.jippy.customerandorder.projection.CoOrderItemProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoOrderItemRepository extends JpaRepository<CoOrderItem,Long> {

// Fetch order items using order id
    List<CoOrderItem> findByOrder_OrderId(String orderId);

//    ======================================================================================
//    ======================================================================================
@Query(value = """
            SELECT
                oi.product_id AS "productId",
                oi.variant_option_id AS "variantOptionId",
                oi.quantity AS "quantity",
                oi.online_unit_price AS "onlineUnitPrice",
                oi.online_price_total AS "onlinePriceTotal"

            FROM jippy_customer_and_order.order_items oi

            WHERE oi.order_id = :orderId

            ORDER BY oi.order_item_id
            """,
        nativeQuery = true)
    List<CoOrderItemProjection> getOrderItems(@Param("orderId") String orderId);
}
