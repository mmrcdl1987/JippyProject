package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmSubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FmSubscriptionPlanRepository extends JpaRepository<FmSubscriptionPlan, Integer> {

    List<FmSubscriptionPlan> findByAreaIdOrderByPriceAsc(Integer areaId);
}