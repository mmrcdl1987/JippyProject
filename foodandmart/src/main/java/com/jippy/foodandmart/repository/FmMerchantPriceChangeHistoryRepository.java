package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmMerchantPriceChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FmMerchantPriceChangeHistoryRepository
        extends JpaRepository<FmMerchantPriceChangeHistory, Integer> {
}