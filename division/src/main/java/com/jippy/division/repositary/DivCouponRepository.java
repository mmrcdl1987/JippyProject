package com.jippy.division.repositary;

import com.jippy.division.entity.DivCoupon;
import com.jippy.division.projection.ActiveCouponProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DivCouponRepository extends JpaRepository<DivCoupon, Integer> {

    Optional<DivCoupon> findByCouponCode(String couponCode);

    boolean existsByCouponCode(String couponCode);

    Page<DivCoupon> findAllByIsActive(Boolean isActive, Pageable pageable);

    List<DivCoupon> findByIsActiveTrue();

    @Query("""
        SELECT c
        FROM DivCoupon c
        WHERE c.isActive = true
          AND c.userType = 'CUSTOMER'
          AND (
                c.startTime IS NULL
                OR c.startTime <= CURRENT_TIMESTAMP
              )
          AND (
                c.endTime IS NULL
                OR c.endTime >= CURRENT_TIMESTAMP
              )
          AND c.couponCode IN ('WELCOME100','WELCOME75','WELCOME50')
        ORDER BY c.discountValue DESC
        """)
    List<DivCoupon> findActiveWelcomeCoupons();

    @Query(value = """
        SELECT
            c.coupon_id AS "sourceId",
            c.coupon_code AS "couponCode",
            c.discount_value AS "discountValue",
            c.min_order_value AS "minimumOrderValue",
            c.start_time AS "startDateTime",
            c.end_time AS "endDateTime",
            CASE
                WHEN c.price_model_id = 1 THEN 'FLAT'
                WHEN c.price_model_id = 2 THEN 'PERCENTAGE'
                ELSE 'UNKNOWN'
            END AS "discountType"
        FROM jippy_division.coupons c
        WHERE c.is_active = TRUE
          AND c.user_type = 'CUSTOMER'
          AND (
                c.start_time IS NULL
                OR c.start_time <= CURRENT_TIMESTAMP
              )
          AND (
                c.end_time IS NULL
                OR c.end_time >= CURRENT_TIMESTAMP
              )
          AND NOT EXISTS (
                SELECT 1
                FROM jippy_division.coupon_mapping_outlets_products cm
                WHERE cm.coupon_id = c.coupon_id
              )
        """, nativeQuery = true)
    List<ActiveCouponProjection> findActiveUnmappedCoupons();

    boolean existsByCouponIdAndIsActive(
            Integer couponId,
            Boolean isActive);
}