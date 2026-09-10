package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmApprovalTransaction;
import com.jippy.foodandmart.projections.FmApprovalTransactionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for Approval Transactions.
 */
public interface FmApprovalTransactionRepository extends JpaRepository<FmApprovalTransaction, Integer> {

    /**
     * Counts DISTINCT Approvers who have approved
     * the same Entity at the same Approval Level.
     *
     * <p>
     * DISTINCT is important because one Approver
     * must contribute only one approval toward the
     * PARALLEL approval threshold.
     *
     * @param entityType    Entity Type
     * @param entityId      Entity Id
     * @param approvalLevel Approval Level
     * @return Number of unique completed approvals
     */
    @Query(value = """
        SELECT COUNT(DISTINCT at.approved_by)
        FROM jippy_fm.approval_transactions at
        WHERE UPPER(at.entity_type) = UPPER(:entityType)
          AND at.entity_id = :entityId
          AND UPPER(at.approval_level) = UPPER(:approvalLevel)
          AND UPPER(at.status) = 'APPROVED'
        """,
            nativeQuery = true)
    Integer countDistinctApprovedApprovers(
            @Param("entityType") String entityType,
            @Param("entityId") Integer entityId,
            @Param("approvalLevel") String approvalLevel);

    /**
     * Checks whether the same Approver has already
     * approved the same Entity at the same Approval Level.
     *
     * <p>
     * Used to prevent duplicate approvals from the
     * same Approver.
     */
    boolean existsByEntityTypeIgnoreCaseAndEntityIdAndApprovalLevelIgnoreCaseAndApprovedByAndStatusIgnoreCase(
            String entityType,
            Integer entityId,
            String approvalLevel,
            Integer approvedBy,
            String status);

    /**
     * Fetches all Approval Transactions by status (case-insensitive), ordered by approvedAt descending.
     *
     * @param status The status to filter by (e.g., "REJECTED", "APPROVED", "PENDING")
     * @return List of approval transactions with the specified status
     */
    List<FmApprovalTransaction> findByStatusIgnoreCaseOrderByApprovedAtDesc(String status);

//    ==============================================================================
@Query(value = """
        SELECT
            at.approval_transactions_id AS "approvalTransactionsId",
            at.entity_type AS "entityType",
            at.entity_id AS "entityId",
            at.approval_level AS "approvalLevel",
            at.status AS "status",
            at.rejected_reason AS "rejectedReason",
            at.approved_by AS "approvedBy",
            at.approved_at AS "approvedAt",
            at.updated_by AS "updatedBy",
            at.updated_at AS "updatedAt",
            e.employee_name AS "approverName"

        FROM jippy_fm.approval_transactions at

        LEFT JOIN jippy_fm.employees e
               ON at.approved_by = e.employee_id

        ORDER BY at.approved_at DESC
        """, nativeQuery = true)
List<FmApprovalTransactionProjection> getAllTransactions();

//======================================================================

}