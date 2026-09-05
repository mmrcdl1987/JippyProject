package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrderPriceBreakup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoOrderPriceBreakupRepository
        extends JpaRepository<CoOrderPriceBreakup, Integer> {

    /**
     * Fetch price breakup using order ID.
     */
    CoOrderPriceBreakup findByOrder_OrderId(String orderId);
}