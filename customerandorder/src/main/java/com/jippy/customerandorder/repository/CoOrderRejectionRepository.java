package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrderRejection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CoOrderRejectionRepository
        extends JpaRepository<CoOrderRejection, Integer> {

    boolean existsByOrderId(String orderId);
}