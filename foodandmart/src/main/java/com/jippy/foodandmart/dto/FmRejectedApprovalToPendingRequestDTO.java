package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Request DTO used to change a rejected Approval Request
 * back to PENDING status.
 *
 * <p>
 * The Approval Request must currently have status REJECTED.
 */
@Data
@Schema(
        name = "FmRejectedApprovalToPendingRequestDTO",
        description = "Request details for changing a REJECTED Approval Request back to PENDING."
)
public class FmRejectedApprovalToPendingRequestDTO {

    /**
     * Approval Request Id that needs to be moved
     * from REJECTED status back to PENDING.
     */
    @NotNull(message = "Approval Request Id is mandatory.")
    @Positive(message = "Approval Request Id must be greater than zero.")
    @Schema(
            description = "Approval Request Id to be moved from REJECTED to PENDING.",
            example = "8",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer approvalRequestId;

    /**
     * User Id /ApproverId - performing the update.
     *
     * Used for audit information in updated_by.
     */
    @NotNull(message = "Updated By is mandatory.")
    @Positive(message = "Updated By must be greater than zero.")
    @Schema(
            description = "User Id performing the update.",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer updatedBy;
}