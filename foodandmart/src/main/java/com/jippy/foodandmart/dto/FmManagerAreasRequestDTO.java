package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request DTO for assigning one Manager
 * to multiple Areas.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Request DTO for assigning a Manager to multiple Areas.")
public class FmManagerAreasRequestDTO {

    /**
     * User Id of the Manager.
     */
    @Schema(description = "Unique identifier of the Manager.", example = "14")
    @NotNull(message = "User Id is required.")
    @Positive(message = "User Id must be greater than zero.")
    private Integer userId;

    /**
     * List of Area Ids to be assigned to the Manager.
     * EX: FLEET MANAGER.
     */
    @Schema(description = "List of Area Ids to be assigned to the Manager.", example = "[1,2,3,4]")
    @NotEmpty(message = "Area Id list cannot be empty.")
    private List<@NotNull(message = "Area Id cannot be null.")
    @Positive(message = "Area Id must be greater than zero.")
            Integer> areaIds;

}