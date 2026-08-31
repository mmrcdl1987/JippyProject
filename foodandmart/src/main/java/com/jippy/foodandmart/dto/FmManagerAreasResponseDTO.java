package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO returned after successfully
 * assigning one Manager to multiple Areas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing the assigned Manager and Area details.")
public class FmManagerAreasResponseDTO {

    /**
     * User Id of the Manager.
     */
    @Schema(description = "Unique identifier of the Manager.", example = "14")
    private Integer userId;

    /**
     * Approver name.
     */
    @Schema(description = "Name of the approver/manager.", example = "John Doe")
    private String approverName;

    /**
     * List of successfully assigned Area Ids.
     */
    @Schema(description = "List of Area Ids assigned to the Manager.", example = "[1,2,3,4]")
    private List<Integer> assignedAreaIds;

}