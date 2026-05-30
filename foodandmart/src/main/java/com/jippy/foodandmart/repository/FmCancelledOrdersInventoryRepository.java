package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmCancelledOrdersInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FmCancelledOrdersInventoryRepository
        extends JpaRepository<
        FmCancelledOrdersInventory,
        Integer> {

    boolean existsByCancelledOrderId(
            String cancelledOrderId);
}