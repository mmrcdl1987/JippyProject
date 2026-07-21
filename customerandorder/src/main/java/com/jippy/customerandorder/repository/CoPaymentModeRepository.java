package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoPaymentModes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoPaymentModeRepository extends JpaRepository<CoPaymentModes, Integer> {
}
