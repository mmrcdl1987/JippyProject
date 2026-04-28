package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmOutletAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FmOutletAddressRepository extends JpaRepository<FmOutletAddress, Integer> {
    java.util.Optional<FmOutletAddress> findByJippyAddressId(Integer outletId);
}
