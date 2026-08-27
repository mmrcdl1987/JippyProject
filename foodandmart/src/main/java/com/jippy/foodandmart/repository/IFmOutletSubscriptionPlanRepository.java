package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmOutletSubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFmOutletSubscriptionPlanRepository
        extends JpaRepository<FmOutletSubscriptionPlan, Integer> {



}