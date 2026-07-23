package com.jippy.foodandmart.specification;

import com.jippy.foodandmart.entity.PromotionPlan;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
public final class PromotionPlanSpecification {

    private PromotionPlanSpecification() {
    }

    public static Specification<PromotionPlan> hasOutletId(Integer outletId) {

        return (root, query, cb) -> {

            if (outletId == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("outletId"), outletId);
        };
    }

    public static Specification<PromotionPlan> hasPromotionPlanType(Integer promotionPlanTypeId) {

        return (root, query, cb) -> {

            if (promotionPlanTypeId == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("promotionPlanType")
                            .get("promotionPlanTypesId"),
                    promotionPlanTypeId);
        };
    }

    public static Specification<PromotionPlan> searchByOfferName(String search) {

        return (root, query, cb) -> {

            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(root.get("offerName")),
                    "%" + search.trim().toLowerCase() + "%");
        };
    }
}