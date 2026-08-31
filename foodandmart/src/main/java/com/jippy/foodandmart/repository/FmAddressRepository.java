package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FmAddressRepository
        extends JpaRepository<FmAddress, Integer> {

    Optional<FmAddress> findByJippyAddressId(
            Integer jippyAddressId
    );

    Optional<FmAddress> findByJippyAddressIdAndAddressType(
            Integer jippyAddressId,
            String addressType
    );

    boolean existsByJippyAddressIdAndAddressType(
            Integer jippyAddressId,
            String addressType
    );
}