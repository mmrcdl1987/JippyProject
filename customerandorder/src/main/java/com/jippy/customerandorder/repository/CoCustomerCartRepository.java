package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomerCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoCustomerCartRepository extends JpaRepository<CoCustomerCart, Integer> {
    Optional<CoCustomerCart> findByCustomerIdAndProductId(
            Integer customerId,
            Integer productId
    );
}
