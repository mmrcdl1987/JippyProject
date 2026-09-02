package com.jippy.customerandorder.repository;
import com.jippy.customerandorder.entity.CoOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoOrderItemRepository extends JpaRepository<CoOrderItem,Long> {

// Fetch order items using order id
    List<CoOrderItem> findByOrder_OrderId(String orderId);
}
