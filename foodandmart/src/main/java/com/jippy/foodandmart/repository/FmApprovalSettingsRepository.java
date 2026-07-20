package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmApprovalSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FmApprovalSettingsRepository extends JpaRepository<FmApprovalSettings, Integer> {


    /* Checks whether an Approval Setting already exists for the given Entity Type and Approval Level. */
    boolean existsByEntityTypeAndApprovalLevel(String entityType, String approvalLevel);


    /* Checks whether an Approval Setting exists for the given Approver Id and Entity Type. */
    boolean existsByApproverIdAndEntityType(Integer approverId, String entityType);

/*------------------------------------------------------------------------------------------------*/
    /**
     * Fetch all ACTIVE approval settings configured
     * for the given approver.
     *
     * Example:
     *
     * approverId = 14
     *
     *
     * Returns:
     *
     * OUTLET  Level 1
     * MERCHANT Level 1
     */
    List<FmApprovalSettings> findByApproverIdAndIsActiveTrue(Integer approverId);
}