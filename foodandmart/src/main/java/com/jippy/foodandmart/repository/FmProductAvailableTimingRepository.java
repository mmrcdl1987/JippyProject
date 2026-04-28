package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmProductAvailableTiming;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FmProductAvailableTimingRepository extends JpaRepository<FmProductAvailableTiming, Integer> {
    List<FmProductAvailableTiming> findByProductId(Integer productId);
    void deleteByProductId(Integer productId);
}
