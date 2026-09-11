package com.jippy.driver.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneStatusToggleRequestDto {

    /**
     * ID of the zone whose status needs to be updated.
     */
    @NotNull(message = "Zone ID cannot be null")
    private Integer zoneId;

    /**
     * New status of the zone.
     *
     * Allowed values:
     * Y = Active
     * N = Inactive
     */
    @NotNull(message = "Status cannot be null")
    @Pattern(
            regexp = "Y|N",
            message = "Status must be either Y or N"
    )
    private String status;
}