package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmAddress;
import com.jippy.foodandmart.dto.FmMerchantAddressDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
            SELECT new com.jippy.foodandmart.dto.FmMerchantAddressDto(
                a.addressId,
                a.jippyAddressId,
                a.addressType,
                a.buildingNumber,
                a.road,
                a.landmark,
                a.stateId,
                a.cityId,
                a.areaId,
                s.stateName,
                c.cityName,
                ar.areaName
            )
            FROM FmAddress a
            LEFT JOIN FmState s ON s.stateId = a.stateId
            LEFT JOIN FmCity c ON c.cityId = a.cityId
            LEFT JOIN FmArea ar ON ar.areaId = a.areaId
            WHERE a.jippyAddressId = :merchantId
              AND a.addressType = :addressType
            """)
    Optional<FmMerchantAddressDto> findMerchantAddressWithLocationNames(
            @Param("merchantId") Integer merchantId,
            @Param("addressType") String addressType
    );

    boolean existsByJippyAddressIdAndAddressType(
            Integer jippyAddressId,
            String addressType
    );
}