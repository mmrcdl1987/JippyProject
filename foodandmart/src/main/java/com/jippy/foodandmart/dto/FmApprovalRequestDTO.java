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
 * Request DTO for creating Approval Requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmApprovalRequestDTO {

    /**
     * Entity Type.
     */
    @Schema(description = "Entity Type.", example = "DRIVER")
    @NotBlank(message = "Entity Type is required.")
    @Pattern(regexp = "MERCHANT|OUTLET|DRIVER", message = "Entity Type must be MERCHANT, OUTLET or DRIVER.")
    private String entityType;

    /**
     * Entity Id.
     */
    @Schema(description = "Entity Id.", example = "101")
    @NotNull(message = "Entity Id is required.")
    @Positive(message = "Entity Id must be greater than zero.")
    private Integer entityId;

    /**
     * Created By.
     */
    @Schema(description = "Created By User Id.", example = "1")
//    @NotNull(message = "Created By is required.")
    @Positive(message = "Created By must be greater than zero.")
    private Integer createdBy;

}
