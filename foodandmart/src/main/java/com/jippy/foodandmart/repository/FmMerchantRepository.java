package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmMerchant;
import com.jippy.foodandmart.entity.FmMerchantBankDetails;
import com.jippy.foodandmart.projections.FmMerchantWithBankProjection;
import com.jippy.foodandmart.projections.FmPendingMerchantApprovalProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FmMerchantRepository extends JpaRepository<FmMerchant, Integer> {
    Optional<FmMerchant> findByMerchantEmail(String email);

    Optional<FmMerchant> findByMerchantPhone(String phone);

    boolean existsByMerchantEmail(String email);

    boolean existsByMerchantPhone(String phone);


    //    for finding the email in the Merchant table
    Optional<FmMerchant> findByMerchantEmailIgnoreCase(String merchantEmail);

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
    FmMerchantWithBankProjection getMerchantWithBank(@Param("merchantId") Integer merchantId);

//-----------------------------------FOR APPROVALS-----------------------------------------------------------------

    /**
     * Fetches all Pending Merchant Approval Requests
     * assigned to the specified Approver and Entity Type.
     * <p>
     * Business Flow:
     * <p>
     * Approval Settings
     * ↓
     * Employee
     * ↓
     * Employee Address (EMPLOYEE)
     * ↓
     * Area
     * ↓
     * Merchant Address (MERCHANT)
     * ↓
     * Merchant
     * <p>
     * Returns only:
     * • Pending Merchants (is_approved = false)
     * • Created within last 24 Hours
     */
    @Query(value = """
            
            SELECT
            
                m.merchant_id                AS merchantId,   --From Merchants Table
                m.merchant_name              AS merchantName,
                m.merchant_email             AS merchantEmail,
                m.merchant_phone             AS merchantPhone,
                m.merchant_business_type     AS merchantBusinessType,
                m.is_approved                AS isApproved,
                m.created_at                 AS createdAt
            
            FROM jippy_fm.approval_settings aps
            
            /* Fetch configured Approver */
            INNER JOIN jippy_fm.employees emp
                    ON emp.employee_id = aps.approver_id
            
            /* Fetch Employee Address */
            INNER JOIN jippy_fm.address emp_addr
                    ON emp_addr.jippy_address_id = emp.employee_id
                   AND emp_addr.address_type = 'EMPLOYEE'
            
            /* Fetch Merchant Addresses from same Area */
            INNER JOIN jippy_fm.address merchant_addr
                    ON merchant_addr.area_id = emp_addr.area_id
                   AND merchant_addr.address_type = 'MERCHANT'
            
            /* Fetch Merchant Details */
            INNER JOIN jippy_fm.merchants m
                    ON m.merchant_id = merchant_addr.jippy_address_id
            
            WHERE aps.approver_id = :approverId
            
                    AND aps.entity_type = :entityType
            
                    AND aps.is_active = TRUE
            
                    AND m.is_approved = FALSE
            
                    AND m.created_at >= NOW() - INTERVAL '24 HOURS'
            
                    ORDER BY m.created_at DESC
            
            """, nativeQuery = true)
    List<FmPendingMerchantApprovalProjection> getPendingMerchantApprovalRequestsByEntityType(
            @Param("approverId") Integer approverId,
            @Param("entityType") String entityType);

//

    /**
     * Approve Merchant.
     */
    @Modifying
    @Query("""
            UPDATE FmMerchant
            SET isApproved = true
            WHERE merchantId = :merchantId
            """)
    int approveMerchant(
            @Param("merchantId") Integer merchantId);

}

