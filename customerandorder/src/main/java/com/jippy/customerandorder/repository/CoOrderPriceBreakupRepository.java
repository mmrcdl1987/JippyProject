package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrderPriceBreakup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoOrderPriceBreakupRepository extends JpaRepository<CoOrderPriceBreakup,Long> {

//    Fetch price breakup using orderId
    CoOrderPriceBreakup findByOrderId(String orderId);

}
