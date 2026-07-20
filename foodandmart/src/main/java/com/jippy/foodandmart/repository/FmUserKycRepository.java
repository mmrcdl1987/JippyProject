package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmUserKyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FmUserKycRepository extends JpaRepository<FmUserKyc, Integer> {
    //Optional<MerchantKyc> findByMerchantId(Integer merchantId);
    boolean existsByPanNumber(String panNumber);
    boolean existsByAadhaarNumber(String aadhaarNumber);
    boolean existsByFssaiNumber(String fssaiNumber);
    Optional<FmUserKyc> findByEntityIdAndEntityType(
            Integer entityId,
            String entityType
    );

    List<FmUserKyc> findAllByEntityType(String entityType);
}
