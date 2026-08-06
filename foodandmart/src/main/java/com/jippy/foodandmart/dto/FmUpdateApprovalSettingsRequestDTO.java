package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating Approval Settings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating Approval Settings.")
public class FmUpdateApprovalSettingsRequestDTO {

    /**
     * Approval Settings Id.
     */
    @Schema(
            description = "Approval Settings Id",
            example = "21",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Approval Settings Id is required.")
    @Positive(message = "Approval Settings Id must be greater than zero.")
    private Integer approvalSettingsId;

    /**
     * New Approver Id.
     */
    @Schema(
            description = "New Approver Id",
            example = "81",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Approver Id is required.")
    @Positive(message = "Approver Id must be greater than zero.")
    private Integer approverId;

    /**
     * Updated By.
     */
    @Schema(
            description = "User Id updating the Approval Settings",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Updated By is required.")
    @Positive(message = "Updated By must be greater than zero.")
    private Integer updatedBy;
}