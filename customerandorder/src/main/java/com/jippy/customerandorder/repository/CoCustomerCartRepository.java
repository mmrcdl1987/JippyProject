package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomerCart;
import com.jippy.customerandorder.projection.CoCartReminderProjection;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CoCustomerCartRepository extends JpaRepository<CoCustomerCart, Integer> {
    Optional<CoCustomerCart> findByCustomerIdAndProductId(
            Integer customerId,
            Integer productId
    );

    List<CoCustomerCart> findByCustomerId(Integer customerId);

    @Transactional
    void deleteByCustomerId(Integer customerId);

    @Query(value = """
        SELECT
            cc.customer_id AS customerId,
            SUM(cc.total_price) AS cartTotal,
            MAX(COALESCE(cc.updated_at, cc.created_at)) AS lastUpdated
        FROM jippy_customer_and_order.customer_cart cc
        GROUP BY cc.customer_id
        HAVING
            MAX(COALESCE(cc.updated_at, cc.created_at))
            <= NOW() - INTERVAL '30 minutes'
        """, nativeQuery = true)
    List<CoCartReminderProjection> findEligibleCartReminders();

    @Query("""
        SELECT c
        FROM CoCustomerCart c
        WHERE c.customerId = :customerId
          AND c.productId = :productId
          AND (
                (:variantOptionId IS NULL AND c.variantOptionId IS NULL)
                OR c.variantOptionId = :variantOptionId
              )
        """)
    Optional<CoCustomerCart> findByCustomerIdAndProductIdAndVariantOptionId(
            @Param("customerId") Integer customerId,
            @Param("productId") Integer productId,
            @Param("variantOptionId") Integer variantOptionId
    );


}
