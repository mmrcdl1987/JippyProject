package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmMerchantBankDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FmMerchantBankDetailsRepository extends JpaRepository<FmMerchantBankDetails, Integer> {
    Optional<FmMerchantBankDetails> findByRecipientId(Integer recipientId);

    boolean existsByAccountNumber(String accountNumber);

    Optional<FmMerchantBankDetails> findByRecipientId(Long recipientId);


    Optional<FmMerchantBankDetails> findByRecipientIdAndUserType(Integer recipientId, String userType);

    Optional<FmMerchantBankDetails> findByAccountNumber(String accountNumber);

    /*
     * Checks whether the given account number is already associated
     * with an outlet belonging to a different merchant.
     *
     * Validate duplicate account number.
     *
     * Same Merchant + Same Account Number      -> Allowed
     * Same Merchant + Different Account Number -> Allowed
     * Different Merchant + Same Account Number -> Not Allowed
     * <> == !=
     */
    @Query(value = """
    SELECT EXISTS (
        SELECT 1
        FROM jippy_fm.user_bank_details ubd
        JOIN jippy_fm.outlets o
             ON o.outlet_id = ubd.recipient_id
        WHERE ubd.user_type = 'OUTLET'
          AND ubd.account_number = :accountNumber
          AND o.merchant_id <> :merchantId
    )
    """, nativeQuery = true)
    boolean existsAccountNumberForAnotherMerchant(
            @Param("accountNumber") String accountNumber,
            @Param("merchantId") Integer merchantId);

}
