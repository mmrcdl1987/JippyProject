package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmOutletAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface FmOutletAddressRepository extends JpaRepository<FmOutletAddress, Integer> {

    Optional<FmOutletAddress> findById(Integer addressId);

    Optional<FmOutletAddress> findByJippyAddressId(Integer jippyAddressId);

    Optional<FmOutletAddress> findByJippyAddressIdAndAddressType(
            Integer jippyAddressId,
            String addressType);

    boolean existsByJippyAddressIdAndAddressType(Integer jippyAddressId, String addressType);

}