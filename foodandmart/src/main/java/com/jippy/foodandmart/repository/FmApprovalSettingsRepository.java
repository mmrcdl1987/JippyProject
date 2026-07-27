package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmApprovalSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FmApprovalSettingsRepository extends JpaRepository<FmApprovalSettings, Integer> {

    /**
     * Checks whether an exact Approval Setting already exists.
     *
     * <p>
     * An Approval Setting is considered duplicate only when
     * all the following fields are the same:
     *
     * 1. Entity Type
     * 2. Approval Level
     * 3. Approver Id
     * 4. Workflow Type
     *
     * <p>
     * Multiple Approvers are allowed for the same
     * Entity Type and Approval Level.
     *
     * @param entityType    Entity Type
     * @param approvalLevel Approval Level
     * @param approverId    Approver Id
     * @param workflowType  Workflow Type
     * @return true if the exact configuration already exists
     */
    boolean existsByEntityTypeAndApprovalLevelAndApproverIdAndWorkflowType(
            String entityType,
            String approvalLevel,
            Integer approverId,
            String workflowType);


    /* Checks whether an Approval Setting exists for the given Approver Id and Entity Type. */
    boolean existsByApproverIdAndEntityType(Integer approverId, String entityType);

    /*------------------------------------------------------------------------------------------------*/

    /**
     * Fetch all ACTIVE approval settings configured
     * for the given approver.
     * <p>
     * Example:
     * <p>
     * approverId = 14
     * <p>
     * <p>
     * Returns:
     * <p>
     * OUTLET  Level 1
     * MERCHANT Level 1
     */
    List<FmApprovalSettings> findByApproverIdAndIsActiveTrue(Integer approverId);

//    ----------------------------------For Update Approvals API-------------------------------------------------------------

    /**
     * Fetch Maximum Approval Level
     * Example :
     * Level 3
     */
    @Query(value = """
            SELECT approval_level
            FROM jippy_fm.approval_settings
            WHERE entity_type = :entityType
            ORDER BY CAST(REPLACE(approval_level,'Level ','') AS INTEGER) DESC
            LIMIT 1
            """, nativeQuery = true)
    String findMaximumApprovalLevel(
            @Param("entityType") String entityType);

    /**
     * Fetch Trigger Activation.
     */
    @Query("""
            SELECT a.triggersActivation
            FROM FmApprovalSettings a
            WHERE a.entityType = :entityType
            AND a.approvalLevel = :approvalLevel
            """)
    Boolean findTriggerActivation(
            @Param("entityType") String entityType,
            @Param("approvalLevel") String approvalLevel);

    /**
     * Fetch Next Approval Level.
     */
    @Query(value = """
            SELECT approval_level
            FROM jippy_fm.approval_settings
            WHERE entity_type=:entityType
            AND CAST(REPLACE(approval_level,'Level ','') AS INTEGER)=
            (
            CAST(REPLACE(:currentLevel,'Level ','') AS INTEGER)+1
            )
            LIMIT 1
            """, nativeQuery = true)
    String findNextApprovalLevel(
            @Param("entityType") String entityType,
            @Param("currentLevel") String currentLevel);

//---------------------------------------------------------------------------------------------------
    /**
     * Checks whether an Active Approval Setting exists
     * for the given Approver ID.
     *
     * @param approverId Approver Employee ID
     * @return true if exists, otherwise false
     */
    boolean existsByApproverIdAndIsActiveTrue(Integer approverId);

    /**
     * Fetches the active Approval Setting configured for the
     * given Entity Type, Approval Level and Approver.
     *
     * <p>
     * Used during Approval processing to identify:
     * - Workflow Type
     * - Required Approvals Count
     * - Current Approver configuration
     *
     * @param entityType    Entity Type such as OUTLET, MERCHANT or DRIVER
     * @param approvalLevel Current Approval Level
     * @param approverId    Approver Id
     * @return Active Approval Setting if configured
     */
    Optional<FmApprovalSettings> findByEntityTypeAndApprovalLevelAndApproverIdAndIsActiveTrue(
            String entityType,
            String approvalLevel,
            Integer approverId);

    /**
     * Counts all DISTINCT active Approvers configured
     * for the same Entity Type and Approval Level
     * under PARALLEL workflow.
     *
     * <p>
     * Business Rule:
     * When required_approvals_count = 0,
     * ALL active PARALLEL Approvers must approve.
     *
     * @param entityType    Entity Type
     * @param approvalLevel Approval Level
     * @return Total number of active PARALLEL Approvers
     */
    @Query(value = """
        SELECT COUNT(DISTINCT aps.approver_id)
        FROM jippy_fm.approval_settings aps
        WHERE UPPER(aps.entity_type) = UPPER(:entityType)
          AND UPPER(aps.approval_level) = UPPER(:approvalLevel)
          AND UPPER(aps.workflow_type) = 'PARALLEL'
          AND aps.is_active = true
        """,
            nativeQuery = true)
    Integer countActiveParallelApprovers(
            @Param("entityType") String entityType,
            @Param("approvalLevel") String approvalLevel);
}