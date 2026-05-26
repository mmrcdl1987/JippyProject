package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomerDeliveryAddress;
import com.jippy.customerandorder.projection.CustomerLocationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CoCustomerDeliveryAddressRepository
        extends JpaRepository<CoCustomerDeliveryAddress, Integer> {

    @Query(value = """
            SELECT
                ST_Y(location::geometry) AS latitude,
                ST_X(location::geometry) AS longitude
            FROM jippy_customer_and_order.customer_delivery_addresses
            WHERE customer_address_id = :customerAddressId
            """, nativeQuery = true)
    CustomerLocationProjection getCustomerLocation(
            @Param("customerAddressId")
            Integer customerAddressId);
}