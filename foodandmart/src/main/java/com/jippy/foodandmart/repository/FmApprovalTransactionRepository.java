package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmApprovalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

}