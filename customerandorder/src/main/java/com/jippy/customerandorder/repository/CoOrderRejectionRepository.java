package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrderRejection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface CoOrderRejectionRepository extends JpaRepository<CoOrderRejection, Integer> {

    boolean existsByOrderId(String orderId);

    //    this query is used to fetch the count of rejected orders for a driver,
//    which will be used in CoDriverTotalEarningsDto
    @Query(value = """
            SELECT COUNT(order_rejection_id)
            
            FROM jippy_customer_and_order.order_rejection
            
            WHERE rejected_by_id = :driverId
            
            AND type = 'DRIVER'
            """, nativeQuery = true)
    Long fetchRejectedOrdersCount(@Param("driverId") Integer driverId);
}