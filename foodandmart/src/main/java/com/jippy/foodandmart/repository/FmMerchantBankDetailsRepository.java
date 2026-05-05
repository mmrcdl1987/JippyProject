package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmMerchantBankDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FmMerchantBankDetailsRepository extends JpaRepository<FmMerchantBankDetails, Integer> {
    Optional<FmMerchantBankDetails> findByRecipientId(Integer recipientId);
    boolean existsByAccountNumber(String accountNumber);
    Optional<FmMerchantBankDetails> findByRecipientId(Long recipientId);
    //boolean existsByAccountNumber(String accountNumber);
    Optional<FmMerchantBankDetails> findByRecipientIdAndUserType(Long recipientId, String userType);

}
