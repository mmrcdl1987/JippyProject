package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmSubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FmSubscriptionPlanRepository extends JpaRepository<FmSubscriptionPlan, Integer> {

    List<FmSubscriptionPlan> findByAreaIdOrderByPriceAsc(Integer areaId);
}