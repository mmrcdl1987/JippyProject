package com.jippy.division.repositary;

import com.jippy.division.entity.DivCoupon;
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

    boolean existsByCouponIdAndIsActive(
            Integer couponId,
            Boolean isActive);
}
