package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomerDeliveryAddress;
import com.jippy.customerandorder.projection.CustomerDeliveryAddressProjection;
import com.jippy.customerandorder.projection.CustomerLocationProjection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoCustomerDeliveryAddressRepository extends JpaRepository<CoCustomerDeliveryAddress, Integer> {

    /**
     * Checks whether the same customer delivery address
     * already exists for the given customer.
     *
     * Used to prevent duplicate customer delivery addresses.
     */
    boolean existsByCustomerIdAndDoorNoAndBuildingNameAndLaneNoAndAreaAndCity(
            Integer customerId,
            String doorNo,
            String buildingName,
            String laneNo,
            Integer area,
            Integer city);
    //     to get the customer location based on the customer address id
    @Query(value = """
            SELECT
                ST_Y(location::geometry) AS latitude,
                ST_X(location::geometry) AS longitude
            FROM jippy_customer_and_order.customer_delivery_addresses
            WHERE customer_address_id = :customerAddressId
            """, nativeQuery = true)
    CustomerLocationProjection getCustomerLocation(@Param("customerAddressId") Integer customerAddressId);

    //    to get all the delivery addresses of a customer based on the customer id
    List<CoCustomerDeliveryAddress> findByCustomerId(Integer customerId);

    @Query(value = """
            SELECT da.customer_address_id as customerAddressId, da.customer_id as customerId,
             da.door_no as doorNo, da.building_name as buildingName, da.lane_no as laneNo, da.area as area,
                ST_Y(location::geometry) AS latitude,
                ST_X(location::geometry) AS longitude
            FROM jippy_customer_and_order.customer_delivery_addresses da
            WHERE customer_address_id = :deliveryAddressId
            """, nativeQuery = true)
    CustomerDeliveryAddressProjection findByDeliveryAddressId(@Param("deliveryAddressId") Integer deliveryAddressId);

    @Query("""
        SELECT a.city
        FROM CoCustomerDeliveryAddress a
        WHERE a.customerAddressId = :customerAddressId
        AND a.customerId = :customerId
        """)
    Integer findCityByCustomerAddressId(
            @Param("customerAddressId") Integer customerAddressId,
            @Param("customerId") Integer customerId
    );
}