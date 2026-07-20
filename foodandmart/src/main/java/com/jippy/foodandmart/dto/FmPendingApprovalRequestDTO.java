package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for fetching pending approvals
 * assigned to a particular approver.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmPendingApprovalRequestDTO {

    @Schema(description = "Employee Id of the approver responsible for approving entities.",
            example = "14")
    @NotNull(message = "Approver Id is required")
    @Positive(message = "Approver Id must be greater than zero")
    private Integer approverId;

    @Schema(description = "Entity type for which pending approvals should be fetched." +
            " Allowed values: OUTLET, MERCHANT, DRIVER.", example = "OUTLET")
    @NotBlank(message = "Entity Type is required")
    @Pattern(regexp = "OUTLET|MERCHANT|DRIVER",
            message = "Entity Type must be OUTLET, MERCHANT, or DRIVER")
    private String entityType;

}