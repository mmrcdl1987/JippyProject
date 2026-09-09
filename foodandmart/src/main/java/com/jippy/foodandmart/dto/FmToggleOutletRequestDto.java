package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO used to toggle the outlet status.
 *
 * The API accepts:
 * 1. outletId  - ID of the outlet to update
 * 2. isToggle  - New toggle value (true / false)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "Toggle Outlet Request",
        description = "Request model used to enable or disable the toggle status of an outlet"
)
public class FmToggleOutletRequestDto {

    /**
     * Outlet ID whose toggle value needs to be updated.
     */
    @Schema(
            description = "Unique ID of the outlet whose toggle status needs to be updated",
            example = "132",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer outletId;

    /**
     * New toggle value for the outlet.
     *
     * true  -> enable toggle
     * false -> disable toggle
     */
    @Schema(
            description = "Toggle status to be applied to the outlet. Set true to enable and false to disable",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean isToggle;
}