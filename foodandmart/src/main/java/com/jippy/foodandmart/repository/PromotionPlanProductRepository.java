package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.PromotionPlanProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionPlanProductRepository
        extends JpaRepository<PromotionPlanProduct, Integer> {

    List<PromotionPlanProduct> findByPromotionPlanPromotionPlanId(
            Integer promotionPlanId);

    List<PromotionPlanProduct> findByProductId(
            Integer productId);

    List<PromotionPlanProduct> findByOutletCategoryId(
            Integer outletCategoryId);

    void deleteByPromotionPlanPromotionPlanId(
            Integer promotionPlanId);
}