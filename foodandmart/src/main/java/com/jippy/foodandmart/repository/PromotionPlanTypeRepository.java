package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.PromotionPlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionPlanTypeRepository extends JpaRepository<PromotionPlanType,Integer> {

    Optional<PromotionPlanType> findByPlanNameIgnoreCase(String planName);

    Optional<PromotionPlanType> findByPlanNameIgnoreCaseAndPromotionPlanTypesIdNot(
            String planName,
            Integer promotionPlanTypesId);
}
