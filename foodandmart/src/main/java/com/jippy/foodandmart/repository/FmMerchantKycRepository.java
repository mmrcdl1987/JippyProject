package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmMerchantKyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FmMerchantKycRepository extends JpaRepository<FmMerchantKyc, Integer> {
    //Optional<MerchantKyc> findByMerchantId(Integer merchantId);
    boolean existsByPanNumber(String panNumber);
    boolean existsByAadhaarNumber(String aadhaarNumber);
    boolean existsByFssaiNumber(String fssaiNumber);
}
