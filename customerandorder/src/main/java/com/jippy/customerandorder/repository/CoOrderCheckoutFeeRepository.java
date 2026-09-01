package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoOrderCheckoutFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoOrderCheckoutFeeRepository
        extends JpaRepository<CoOrderCheckoutFee, Integer> {

    Optional<CoOrderCheckoutFee> findByAreaId(Integer areaId);
    Optional<CoOrderCheckoutFee> findByAreaIdAndOrderCheckoutFeeIdNot(
            Integer areaId,
            Integer orderCheckoutFeeId
    );


}