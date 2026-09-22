package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmMerchant;
import com.jippy.foodandmart.projections.FmAdminMerchantProjection;
import com.jippy.foodandmart.projections.FmMerchantWithBankProjection;
import com.jippy.foodandmart.projections.FmPendingMerchantApprovalProjection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    List<FmMerchant> findByMerchantIdIn(List<Integer> merchantIds);

    Optional<FmMerchant> findByMerchantEmailIgnoreCase(
            String merchantEmail
    );

    boolean existsByMerchantEmail(
            String email
    );


    Optional<FmMerchant> findByMerchantPhone(String merchantPhone);

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
            m.is_approved AS isApproved,

            addr.building_number AS buildingNumber,
            addr.road AS road,
            addr.landmark AS landmark,
            addr.state_id AS stateId,
            st.state_name AS stateName,
            addr.city_id AS cityId,
            ci.city_name AS cityName,
            addr.area_id AS areaId,
            ar.area_name AS areaName,

            u.bank_id AS bankId,
            u.recipient_id AS recipientId,
            u.account_number AS accountNumber,
            u.ifsc_code AS ifscCode,
            u.bank_name AS bankName,
            u.account_holder_name AS accountHolderName,
            u.user_type AS userType,

            k.aadhaar_number AS aadhaarNumber,
            k.pan_number AS panNumber,
            k.aadhaar_number AS aadharNumber,
            k.pan_number AS panNumber,
            k.aadhaar_number_url AS aadhaarNumberUrl,
            k.pan_number_url AS panNumberUrl

        FROM jippy_fm.merchants m

        JOIN jippy_fm.user_bank_details u
          ON u.recipient_id = m.merchant_id
         AND u.user_type = 'MERCHANT'

        LEFT JOIN jippy_fm.address addr
          ON addr.jippy_address_id = m.merchant_id
         AND addr.address_type = 'MERCHANT'

        LEFT JOIN jippy_fm.state st
          ON st.state_id = addr.state_id

        LEFT JOIN jippy_fm.city ci
          ON ci.city_id = addr.city_id

        LEFT JOIN jippy_fm.area ar
          ON ar.area_id = addr.area_id

        LEFT JOIN jippy_fm.user_kyc k
          ON k.entity_id = m.merchant_id
         AND UPPER(k.entity_type) = 'MERCHANT'

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

//    ===================================================================================
//    ===================================================================================
    /**
     * Searches merchants by merchant name using a partial,
     * case-insensitive match.
     *
     * Example:
     * Input "ro" -> Rohan Vadluri
     * Input "PON" -> merchants containing "pon"
     *
     * ILIKE is PostgreSQL-specific and provides case-insensitive
     * searching without requiring the input to match the
     * complete merchant name.
     */
    @Query("""
            SELECT m
            FROM FmMerchant m
            WHERE LOWER(m.merchantName) LIKE LOWER(CONCAT('%', :merchantName, '%'))
            ORDER BY m.merchantName ASC
            """)
    List<FmMerchant> searchByMerchantName(
            @Param("merchantName") String merchantName
    );


    @Query(
            value = """
        SELECT
            m.merchant_id AS merchantId,
            m.merchant_name AS merchantName,
            m.merchant_email AS merchantEmail,
            m.merchant_phone AS merchantPhone,
            m.merchant_business_type AS merchantBusinessType,

            m.status AS status,
            m.is_active AS isActive,
            m.is_approved AS isApproved,

            m.created_at AS createdAt,
            m.created_by AS createdBy,

            m.updated_at AS updatedAt,
            m.updated_by AS updatedBy,

            m.profile_pic_url AS profilePicUrl,

            a.area_id AS areaId,
            ar.area_name AS areaName

        FROM jippy_fm.merchants m

        LEFT JOIN jippy_fm.address a
            ON a.jippy_address_id = m.merchant_id
            AND a.address_type = 'MERCHANT'

        LEFT JOIN jippy_fm.area ar
            ON ar.area_id = a.area_id

        WHERE

            (
                :search IS NULL
                OR :search = ''
                OR LOWER(m.merchant_name)
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(m.merchant_email)
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR m.merchant_phone
                    LIKE CONCAT('%', :search, '%')
            )

            AND
            (
                :merchantBusinessType IS NULL
                OR :merchantBusinessType = ''
                OR LOWER(m.merchant_business_type)
                    = LOWER(:merchantBusinessType)
            )

            AND
            (
                :areaId IS NULL
                OR a.area_id = :areaId
            )

            AND
            (
                :isActive IS NULL
                OR :isActive = ''
                OR UPPER(m.is_active) = UPPER(:isActive)
            )

            AND
            (
                :isApproved IS NULL
                OR m.is_approved = :isApproved
            )

            AND
            (
                :status IS NULL
                OR :status = ''
                OR LOWER(m.status) = LOWER(:status)
            )

        ORDER BY m.created_at DESC
        """,

            countQuery = """
        SELECT COUNT(DISTINCT m.merchant_id)

        FROM jippy_fm.merchants m

        LEFT JOIN jippy_fm.address a
            ON a.jippy_address_id = m.merchant_id
            AND a.address_type = 'MERCHANT'

        WHERE

            (
                :search IS NULL
                OR :search = ''
                OR LOWER(m.merchant_name)
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(m.merchant_email)
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR m.merchant_phone
                    LIKE CONCAT('%', :search, '%')
            )

            AND
            (
                :merchantBusinessType IS NULL
                OR :merchantBusinessType = ''
                OR LOWER(m.merchant_business_type)
                    = LOWER(:merchantBusinessType)
            )

            AND
            (
                :areaId IS NULL
                OR a.area_id = :areaId
            )

            AND
            (
                :isActive IS NULL
                OR :isActive = ''
                OR UPPER(m.is_active) = UPPER(:isActive)
            )

            AND
            (
                :isApproved IS NULL
                OR m.is_approved = :isApproved
            )

            AND
            (
                :status IS NULL
                OR :status = ''
                OR LOWER(m.status) = LOWER(:status)
            )
        """,

            nativeQuery = true
    )
    Page<FmAdminMerchantProjection> findAdminMerchants(

            @Param("search")
            String search,

            @Param("merchantBusinessType")
            String merchantBusinessType,

            @Param("areaId")
            Integer areaId,

            @Param("isActive")
            String isActive,

            @Param("isApproved")
            Boolean isApproved,

            @Param("status")
            String status,

            Pageable pageable
    );


}