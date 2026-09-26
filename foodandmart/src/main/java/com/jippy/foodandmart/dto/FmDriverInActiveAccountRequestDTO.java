package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to deactivate driver account")
public class FmDriverInActiveAccountRequestDTO {

    @Schema(
            description = "Driver ID",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer driverId;
}