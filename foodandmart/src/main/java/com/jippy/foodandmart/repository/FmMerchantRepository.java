package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmMerchant;
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
public interface FmMerchantRepository
        extends JpaRepository<FmMerchant, Integer> {

    // ============================================================
    // EMAIL
    // ============================================================

    Optional<FmMerchant> findByMerchantEmail(
            String email
    );

    Optional<FmMerchant> findByMerchantEmailIgnoreCase(
            String merchantEmail
    );

    boolean existsByMerchantEmail(
            String email
    );


    // ============================================================
    // PHONE
    // ============================================================

    Optional<FmMerchant> findByMerchantPhone(
            String phone
    );

    boolean existsByMerchantPhone(
            String phone
    );


    // ============================================================
    // MERCHANT NAME
    // ============================================================

    /**
     * Finds merchant by name ignoring case and
     * leading/trailing spaces.
     *
     * Example:
     *
     * CSV:
     * mahendra
     *
     * Database:
     * mahendra
     *
     * Result:
     * merchant_id = 35
     */
    @Query("""
            SELECT m
            FROM FmMerchant m
            WHERE LOWER(TRIM(m.merchantName))
                  = LOWER(TRIM(:merchantName))
            """)
    Optional<FmMerchant> findMerchantByName(
            @Param("merchantName") String merchantName
    );


    // ============================================================
    // MERCHANT + BANK DETAILS
    // ============================================================

    @Query(value = """
            SELECT
                m.merchant_id AS merchantId,
                m.merchant_name AS merchantName,
                m.merchant_email AS merchantEmail,
                m.merchant_phone AS merchantPhone,
                m.merchant_business_type AS businessType,
                m.status AS status,

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
            """,
            nativeQuery = true)
    FmMerchantWithBankProjection getMerchantWithBank(
            @Param("merchantId") Integer merchantId
    );


    // ============================================================
    // PENDING MERCHANT APPROVALS
    // ============================================================

    @Query(value = """

            SELECT

                m.merchant_id            AS merchantId,
                m.merchant_name          AS merchantName,
                m.merchant_email         AS merchantEmail,
                m.merchant_phone         AS merchantPhone,
                m.merchant_business_type AS merchantBusinessType,
                m.is_approved            AS isApproved,
                m.created_at             AS createdAt

            FROM jippy_fm.approval_settings aps

            INNER JOIN jippy_fm.employees emp
                    ON emp.employee_id = aps.approver_id

            INNER JOIN jippy_fm.address emp_addr
                    ON emp_addr.jippy_address_id = emp.employee_id
                   AND emp_addr.address_type = 'EMPLOYEE'

            INNER JOIN jippy_fm.address merchant_addr
                    ON merchant_addr.area_id = emp_addr.area_id
                   AND merchant_addr.address_type = 'MERCHANT'

            INNER JOIN jippy_fm.merchants m
                    ON m.merchant_id = merchant_addr.jippy_address_id

            WHERE aps.approver_id = :approverId

              AND aps.entity_type = :entityType

              AND aps.is_active = TRUE

              AND m.is_approved = FALSE

              AND m.created_at >= NOW() - INTERVAL '24 HOURS'

            ORDER BY m.created_at DESC

            """,
            nativeQuery = true)
    List<FmPendingMerchantApprovalProjection>
    getPendingMerchantApprovalRequestsByEntityType(

            @Param("approverId")
            Integer approverId,

            @Param("entityType")
            String entityType
    );


    // ============================================================
    // APPROVE MERCHANT
    // ============================================================

    @Modifying
    @Query("""
            UPDATE FmMerchant
            SET isApproved = true
            WHERE merchantId = :merchantId
            """)
    int approveMerchant(
            @Param("merchantId")
            Integer merchantId
    );
}