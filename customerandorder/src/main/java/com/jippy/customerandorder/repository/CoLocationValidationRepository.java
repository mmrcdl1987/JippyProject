package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CoLocationValidationRepository
        extends JpaRepository<CoOrder, String> {

    @Query(value = """
            SELECT
                ST_Distance(
                    cda.location,
                    ST_SetSRID(
                        ST_MakePoint(:longitude, :latitude),
                        4326
                    )::geography
                )
            FROM jippy_customer_and_order.orders o
            
            JOIN jippy_customer_and_order.customer_delivery_addresses cda
                ON o.customer_delivery_address_id =
                   cda.customer_address_id
                   
            WHERE o.order_id = :orderId
            """, nativeQuery = true)
    Double calculateDistance(
            @Param("orderId") String orderId,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude);
}