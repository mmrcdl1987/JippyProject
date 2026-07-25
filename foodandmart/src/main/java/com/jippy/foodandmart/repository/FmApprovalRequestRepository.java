package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmApprovalRequest;
import com.jippy.foodandmart.projections.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

/**
 * Repository for Approval Requests.
 * <p>
 * Used to perform database operations on the
 * approval_requests table.
 */
@Repository
public interface FmApprovalRequestRepository extends JpaRepository<FmApprovalRequest, Integer> {

    /**
     * Fetches all Level-1 Pending OUTLET Approval Requests.
     * <p>
     * Flow:
     * 1. Fetch all approval requests where
     * - Entity Type = OUTLET
     * - Current Level = Level 1
     * - Status = PENDING
     * <p>
     * 2. Join Outlet table to fetch outlet basic details.
     * <p>
     * 3. Join User KYC table to fetch
     * - GST Number
     * - FSSAI Number
     * <p>
     * 4. Join Address table to fetch outlet address.
     * <p>
     * 5. Join State, City and Area tables
     * to display readable names instead of IDs.
     * <p>
     * 6. Convert PostGIS Geometry(Location)
     * into Latitude and Longitude.
     */
//    ------------------------------- FOR OUTLET APPROVAL---------------------------------------------------
    @Query(value = """
            
            /*==========================================================
             = Fetch Pending Approval Request Details
             ==========================================================*/
            SELECT
            
                ar.approval_request_id                     AS approvalRequestId,
                ar.entity_type                            AS entityType,
                ar.entity_id                              AS entityId,
                ar.current_level                          AS currentLevel,
                ar.status                                 AS status,
            
            /*==========================================================
             = Outlet Basic Details
             ==========================================================*/
            
                o.outlet_id                               AS outletId,
                o.outlet_name                             AS outletName,
                m.merchant_name                           AS merchantName,
                o.cuisine_type                            AS cuisineType,
                o.outlet_phone                            AS outletPhone,
                o.outlet_email                            AS outletEmail,
                o.is_approved                             AS outletApproved,
            
            /*==========================================================
             = Convert PostGIS Location into Latitude & Longitude
             ==========================================================*/
            
                ST_Y(o.outlet_location::geometry)          AS latitude,
                ST_X(o.outlet_location::geometry)          AS longitude,
            
            /*==========================================================
             = Outlet KYC Details
             ==========================================================*/
            
                uk.fssai_number                           AS fssaiNumber,
                uk.gst_number                             AS gstNumber,
            
            /*==========================================================
             = Address Details
             ==========================================================*/
            
                ad.address_id                             AS addressId,
                ad.building_number                        AS buildingNumber,
                ad.road                                   AS road,
                ad.landmark                               AS landmark,
            
            /*==========================================================
             = State / City / Area Names
             ==========================================================*/
            
                st.state_name                             AS stateName,
                ct.city_name                              AS cityName,
                ar1.area_name                             AS areaName
            
            /*==========================================================
             = Approval Requests Table
             ==========================================================*/
            
            FROM jippy_fm.approval_requests ar
            
            /*==========================================================
             = Join Outlet Table
             = approval_requests.entity_id = outlets.outlet_id
             ==========================================================*/
            
            INNER JOIN jippy_fm.outlets o
                   ON ar.entity_id = o.outlet_id
            
            /*==========================================================
             = Join Merchant Table
             = Fetch Merchant Name
             ==========================================================*/
            
              LEFT JOIN jippy_fm.merchants m
                ON o.merchant_id = m.merchant_id 
            
            /*==========================================================
             = Join User KYC Table
             = Fetch only OUTLET KYC Details
             ==========================================================*/
            
            LEFT JOIN jippy_fm.user_kyc uk
                   ON uk.entity_id = o.outlet_id
                  AND uk.entity_type = 'OUTLET'
            
            /*==========================================================
             = Join Address Table
             = Fetch OUTLET Address
             ==========================================================*/
            
            LEFT JOIN jippy_fm.address ad
                   ON ad.jippy_address_id = o.outlet_id
                  AND ad.address_type = 'OUTLET'
            
            /*==========================================================
             = Join State Master
             ==========================================================*/
            
            LEFT JOIN jippy_fm.state st
                   ON st.state_id = ad.state_id
            
            /*==========================================================
             = Join City Master
             ==========================================================*/
            
            LEFT JOIN jippy_fm.city ct
                   ON ct.city_id = ad.city_id
            
            /*==========================================================
             = Join Area Master
             ==========================================================*/
            
            LEFT JOIN jippy_fm.area ar1
                   ON ar1.area_id = ad.area_id
            
            /*==========================================================
             = Apply Filters
             ==========================================================*/
            
            /*==========================================================
             = Apply Approval Request Filters
             ==========================================================*/

              WHERE ar.entity_type = 'OUTLET'
              AND ar.current_level = :approvalLevel
              AND ar.status = 'PENDING'

            /*==========================================================
             = Exclude Requests already approved by THIS Approver
             ==========================================================*/
    
            /*
             * PARALLEL Workflow Rule:
             *
             * Multiple Approvers can be configured for the same
             * Entity Type and Approval Level.
             *
             * If the currently logged-in Approver has already
             * approved this particular OUTLET at this Level,
             * the same request must not be shown to that Approver again.
             *
             * Approval performed by another Approver does NOT
             * exclude the request for the current Approver.
             */
                AND NOT EXISTS (
    
                SELECT 1
    
                FROM jippy_fm.approval_transactions at
    
                WHERE at.entity_id = ar.entity_id
    
              /*------------------------------------------------------
               * Same Entity Type
               *------------------------------------------------------*/
              AND UPPER(at.entity_type) = UPPER(ar.entity_type)

              /*------------------------------------------------------
               * Same Approval Level
               *------------------------------------------------------*/
              AND UPPER(at.approval_level) = UPPER(ar.current_level)

              /*------------------------------------------------------
               * Same Logged-in Approver
               *------------------------------------------------------*/
              AND at.approved_by = :approverId

              /*------------------------------------------------------
               * Only completed manual approvals are considered
               *------------------------------------------------------*/
              AND UPPER(at.status) = 'APPROVED'
            )

            /*==========================================================
             = Latest Pending Requests First
             ==========================================================*/
            
            ORDER BY ar.created_at DESC
            
            """, nativeQuery = true)
    List<FmOutletLevel1PendingApprovalProjection> getOutletLevel1PendingRequests(
            @Param("approvalLevel") String approvalLevel,
            @Param("approverId") Integer approverId);
//    ---------------------------------FOR MERCHANT APPROVAL------------------------------------------------------------

    /**
     * Fetches all Level-1 Pending MERCHANT Approval Requests.
     * <p>
     * Flow:
     * 1. Fetch all approval requests where
     * - Entity Type = MERCHANT
     * - Current Level = Level 1
     * - Status = PENDING
     * <p>
     * 2. Join Merchant table to fetch merchant details.
     * <p>
     * 3. Join User KYC table to fetch
     * - Aadhaar Number
     * - PAN Number
     */
    @Query(value = """
             /*==========================================================
              = Fetch Pending Merchant Approval Request Details
              ==========================================================*/
            
             SELECT
            
             /*==========================================================
              = Approval Request Details
              ==========================================================*/
            
                 ar.approval_request_id                     AS approvalRequestId,
                 ar.entity_type                            AS entityType,
                 ar.entity_id                              AS entityId,
                 ar.current_level                          AS currentLevel,
                 ar.status                                 AS status,
            
            /*==========================================================
             = Merchant Details
             ==========================================================*/
            
                m.merchant_id                             AS merchantId,
                m.merchant_name                           AS merchantName,
                m.merchant_email                          AS merchantEmail,
                m.merchant_phone                          AS merchantPhone,
                m.merchant_business_type                  AS merchantBusinessType,
                m.is_approved                             AS merchantApproved,
                m.profile_pic_url                         AS merchantProfilePicUrl,
             /*==========================================================
              = Merchant KYC Details
              ==========================================================*/
            
                 uk.aadhaar_number                         AS aadhaarNumber,
                 uk.pan_number                             AS panNumber,
            
             /*==========================================================
              = Address Details
              ==========================================================*/
            
                 ad.address_id                             AS addressId,
                 ad.building_number                        AS buildingNumber,
                 ad.road                                   AS road,
                 ad.landmark                               AS landmark,
            
             /*==========================================================
              = State / City / Area
              ==========================================================*/
            
                 st.state_name                             AS stateName,
                 ct.city_name                              AS cityName,
                 ar1.area_name                             AS areaName
            
             FROM jippy_fm.approval_requests ar
            
             /*==========================================================
              = Merchant Table
              ==========================================================*/
            
             INNER JOIN jippy_fm.merchants m
                     ON ar.entity_id = m.merchant_id    
            
             /*==========================================================
              = Merchant KYC
              ==========================================================*/
            
             LEFT JOIN jippy_fm.user_kyc uk
                    ON uk.entity_id = m.merchant_id
                   AND uk.entity_type = 'MERCHANT'
            
             /*==========================================================
              = Merchant Address
              ==========================================================*/
            
             LEFT JOIN jippy_fm.address ad
                    ON ad.jippy_address_id = m.merchant_id
                   AND ad.address_type = 'MERCHANT'
            
             /*==========================================================
              = State
              ==========================================================*/
            
             LEFT JOIN jippy_fm.state st
                    ON st.state_id = ad.state_id
            
             /*==========================================================
              = City
              ==========================================================*/
            
             LEFT JOIN jippy_fm.city ct
                    ON ct.city_id = ad.city_id
            
             /*==========================================================
              = Area
              ==========================================================*/
            
             LEFT JOIN jippy_fm.area ar1
                    ON ar1.area_id = ad.area_id
            
             /*==========================================================
              = Filters
              ==========================================================*/

             WHERE ar.entity_type = 'MERCHANT'
              AND ar.current_level = :approvalLevel
              AND ar.status = 'PENDING'

            /*==========================================================
             = Exclude Merchant already approved by THIS Approver
             ==========================================================*/

            /*
             * PARALLEL Approval Rule:
             *
             * If the currently logged-in Approver has already
             * approved this Merchant at this Approval Level,
             * the Merchant must not be shown again to the
             * same Approver.
             *
             * Approval by another Approver does not exclude
             * this Merchant for the current Approver.
             */
              AND NOT EXISTS (

                SELECT 1

                FROM jippy_fm.approval_transactions at

                WHERE at.entity_id = ar.entity_id

              /*------------------------------------------------------
               * Same Entity Type
               *------------------------------------------------------*/
              AND UPPER(at.entity_type) = UPPER(ar.entity_type)

              /*------------------------------------------------------
               * Same Approval Level
               *------------------------------------------------------*/
              AND UPPER(at.approval_level) = UPPER(ar.current_level)

              /*------------------------------------------------------
               * Same Logged-in Approver
               *------------------------------------------------------*/
              AND at.approved_by = :approverId

              /*------------------------------------------------------
               * Transaction must already be APPROVED
               *------------------------------------------------------*/
              AND UPPER(at.status) = 'APPROVED'
            )

            ORDER BY ar.created_at DESC
                
            """, nativeQuery = true)
    List<FmMerchantLevel1PendingApprovalProjection> getMerchantLevel1PendingRequests(
            @Param("approvalLevel") String approvalLevel,
            @Param("approverId") Integer approverId);

    //----------------------------FOR DRIVER APPROVAL-------------------------------------------------
    @Query(value = """
            /*==========================================================
             = Fetch Pending Driver Approval Requests
             ==========================================================*/
            
            SELECT
            
            /*==========================================================
             = Approval Request Details
             ==========================================================*/
            
                ar.approval_request_id     AS approvalRequestId,
                ar.entity_type             AS entityType,
                ar.entity_id               AS entityId,
                ar.current_level           AS currentLevel,
                ar.status                  AS status,
            
            /*==========================================================
             = Driver Id
             ==========================================================*/
            
                ar.entity_id               AS driverId
            
            FROM jippy_fm.approval_requests ar
            
            /*==========================================================
             = Filters
             ==========================================================*/
            
           
            WHERE ar.entity_type = 'DRIVER'
              AND ar.current_level = :approvalLevel
              AND ar.status = 'PENDING'

            /*==========================================================
             = Exclude Driver already approved by THIS Approver
             ==========================================================*/

            /*
             * PARALLEL Approval Rule:
             *
             * If the currently logged-in Approver has already
             * approved this Driver at this Approval Level,
             * the Driver must not be shown again to the
             * same Approver.
             *
             * Approval by another Approver does not exclude
             * this Driver for the current Approver.
             */
            AND NOT EXISTS (

                SELECT 1

                FROM jippy_fm.approval_transactions at

                WHERE at.entity_id = ar.entity_id

              /*------------------------------------------------------
               * Same Entity Type
               *------------------------------------------------------*/
              AND UPPER(at.entity_type) = UPPER(ar.entity_type)

              /*------------------------------------------------------
               * Same Approval Level
               *------------------------------------------------------*/
              AND UPPER(at.approval_level) = UPPER(ar.current_level)

              /*------------------------------------------------------
               * Same Logged-in Approver
               *------------------------------------------------------*/
              AND at.approved_by = :approverId

              /*------------------------------------------------------
               * Transaction must already be APPROVED
               *------------------------------------------------------*/
              AND UPPER(at.status) = 'APPROVED'
            )

            ORDER BY ar.created_at DESC
            
            """, nativeQuery = true)
    List<FmDriverLevel1PendingApprovalProjection> getDriverLevel1PendingRequests(
            @Param("approvalLevel") String approvalLevel,
            @Param("approverId") Integer approverId);
//
//-----------------------------GET DRIVER ADDRESS--------------------------------------------

    /**
     * ===========================================================
     * Fetch Driver Address Details
     * ===========================================================
     * <p>
     * Retrieves Driver Address information from FM database
     * along with State, City and Area names.
     */
    @Query(value = """
            
            /*==========================================================
             = Driver Address Details
             ==========================================================*/
            
            SELECT
            
                a.address_id            AS addressId,
                a.building_number       AS buildingNumber,
                a.road                  AS road,
                a.landmark              AS landmark,
                a.state_id              AS stateId,
                a.city_id               AS cityId,
                a.area_id               AS areaId,
            
            /*==========================================================
             = State Details
             ==========================================================*/
            
                s.state_name            AS stateName,
            
            /*==========================================================
             = City Details
             ==========================================================*/
            
                c.city_name             AS cityName,
            
            /*==========================================================
             = Area Details
             ==========================================================*/
            
                ar.area_name            AS areaName
            
            FROM jippy_fm.address a
            
            /*==========================================================
             = Join State
             ==========================================================*/
            
            LEFT JOIN jippy_fm.state s
                   ON s.state_id = a.state_id
            
            /*==========================================================
             = Join City
             ==========================================================*/
            
            LEFT JOIN jippy_fm.city c
                   ON c.city_id = a.city_id
            
            /*==========================================================
             = Join Area
             ==========================================================*/
            
            LEFT JOIN jippy_fm.area ar
                   ON ar.area_id = a.area_id
            
            /*==========================================================
             = Filters
             ==========================================================*/
            
            WHERE a.jippy_address_id = :driverId
              AND a.address_type = 'DRIVER'
            
            """, nativeQuery = true)
    FmDriverAddressProjection getDriverAddress(@Param("driverId") Integer driverId);

//------------------------For Update Approvals API------------------------------------------------------------------------

    /**
     * Fetch all Approval Requests for the given Approval Request IDs.
     *
     * @param approvalRequestIds List of Approval Request IDs
     * @return List of Approval Request Entities
     */
    List<FmApprovalRequest> findByApprovalRequestIdIn(List<Integer> approvalRequestIds);


    /**
     * Update Current Level.
     */
    @Modifying
    @Query("""
            UPDATE FmApprovalRequest
            SET currentLevel = :currentLevel,
                updatedBy = :updatedBy,
                updatedAt = CURRENT_TIMESTAMP
            WHERE approvalRequestId = :approvalRequestId
            """)
    int updateCurrentLevel(@Param("approvalRequestId") Integer approvalRequestId, @Param("currentLevel") String currentLevel, @Param("updatedBy") Integer updatedBy);

    /**
     * Update Status.
     */
    @Modifying
    @Query("""
            UPDATE FmApprovalRequest
            SET status = :status,
                updatedBy = :updatedBy,
                updatedAt = CURRENT_TIMESTAMP
            WHERE approvalRequestId = :approvalRequestId
            """)
    int updateStatus(@Param("approvalRequestId") Integer approvalRequestId, @Param("status") String status, @Param("updatedBy") Integer updatedBy);

    /**
     * Fetches all pending approval requests
     * eligible for auto approval.
     * <p>
     * Conditions
     * ----------
     * 1. Approval Request Status must be PENDING.
     * 2. Approval Setting must be Active.
     * 3. Entity Type should match.
     * 4. Current Level should match Approval Level.
     * 5. Escalation Time should be completed.
     *
     * @return List of Pending Approval Requests.
     */
    @Query(value = """
                    SELECT
                        r.approval_request_id AS approvalRequestId,
                        r.entity_type AS entityType,
                        r.entity_id AS entityId,
                        r.current_level AS currentLevel,
                        r.status AS status,
                        r.created_at AS createdAt,
                        r.updated_at AS updatedAt,
                        s.approver_id AS approverId,
                        s.time_to_escalate_in_hours AS timeToEscalateInHours,
                        s.triggers_activation AS triggersActivation
                    FROM jippy_fm.approval_requests r
                    JOIN jippy_fm.approval_settings s
                         ON r.entity_type = s.entity_type
                        AND r.current_level = s.approval_level
                    WHERE r.status = 'PENDING'
                      AND s.is_active = TRUE
                      AND s.time_to_escalate_in_hours IS NOT NULL
                      AND EXTRACT(EPOCH FROM (
                                CURRENT_TIMESTAMP - COALESCE(r.updated_at, r.created_at)
                --          )) / 60 >= s.time_to_escalate_in_hours
                      )) / 3600 >= s.time_to_escalate_in_hours
            """, nativeQuery = true)
    List<FmPendingApprovalProjection> findPendingRequestsForAutoApproval();
}