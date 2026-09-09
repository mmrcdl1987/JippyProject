package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoPaymentModes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoPaymentModeRepository extends JpaRepository<CoPaymentModes, Integer> {

    Optional<CoPaymentModes> findByPaymentModeId(Integer paymentModeId);

    List<CoPaymentModes> findByIsActive(String isActive);

    Optional<CoPaymentModes> findByPaymentModeAndIsActive(
            String paymentMode,
            String isActive
    );

    boolean existsByPaymentModeAndIsActive(
            String paymentMode,
            String isActive
    );
}
