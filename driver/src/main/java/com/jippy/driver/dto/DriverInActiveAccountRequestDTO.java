package com.jippy.driver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to deactivate a driver account")
public class DriverInActiveAccountRequestDTO {

    @NotNull
    @Schema(
            description = "Driver ID",
            example = "36",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer driverId;
}