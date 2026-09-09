package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CustomerCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerCouponRepository extends JpaRepository<CustomerCoupon, Integer> {

    List<CustomerCoupon> findByCustomerId(Integer customerId);

    List<CustomerCoupon> findByCustomerIdAndCouponId(
            Integer customerId,
            Integer couponId);

    Optional<CustomerCoupon> findByOrderId(String orderId);

    boolean existsByCustomerIdAndCouponIdAndIsRedeemedTrue(
            Integer customerId,
            Integer couponId);

    long countByCustomerIdAndCouponIdAndIsRedeemedTrue(
            Integer customerId,
            Integer couponId);
}