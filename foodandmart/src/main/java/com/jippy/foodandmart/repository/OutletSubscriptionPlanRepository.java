package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmOutletSubscriptionPlan;
import com.jippy.foodandmart.entity.FmSubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OutletSubscriptionPlanRepository
        extends JpaRepository<FmOutletSubscriptionPlan, Integer> {

    Optional<FmOutletSubscriptionPlan> findByOutletId(Integer outletId);

}