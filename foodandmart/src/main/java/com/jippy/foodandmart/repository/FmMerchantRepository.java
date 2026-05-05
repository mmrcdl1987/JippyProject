package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmMerchant;
import com.jippy.foodandmart.projections.FmMerchantWithBankProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FmMerchantRepository extends JpaRepository<FmMerchant, Integer> {
    Optional<FmMerchant> findByMerchantEmail(String email);
    Optional<FmMerchant> findByMerchantPhone(String phone);
    boolean existsByMerchantEmail(String email);
    boolean existsByMerchantPhone(String phone);


    @Query(value = """
            SELECT 
                m.merchant_id AS merchantId,
                m.merchant_name AS merchantName,
                m.merchant_email AS merchantEmail,
                m.merchant_phone AS merchantPhone,
                m.merchant_business_type AS businessType,
                m.status AS status,
    -- for bank details
               u.bank_id AS bankId,
               u.recipient_id AS recipientId,
               u.account_number AS accountNumber,
               u.ifsc_code AS ifscCode,
               u.bank_name AS bankName,
               u.account_holder_name AS accountHolderName,
               u.user_type AS userType
            FROM jippy_fm.merchants m
             JOIN jippy_fm.user_bank_details u
                ON u.recipient_id = m.merchant_id
                AND u.user_type = 'MERCHANT'
            WHERE m.merchant_id = :merchantId
            """, nativeQuery = true)
//     for fetching bank details from the Merchant-table
    FmMerchantWithBankProjection getMerchantWithBank(@Param("merchantId") Long merchantId);
}
