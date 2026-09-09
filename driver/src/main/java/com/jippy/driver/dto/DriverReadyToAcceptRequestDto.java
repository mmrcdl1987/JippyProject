package com.jippy.driver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO used to update the driver's
 * ready-to-accept-orders status.
 *
 * The API accepts:
 * 1. driverId              - ID of the driver
 * 2. readyToAcceptOrders   - true / false
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "Driver Ready To Accept Request",
        description = "Request model used to enable or disable a driver's readiness to accept orders"
)
public class DriverReadyToAcceptRequestDto {

    /**
     * Driver ID whose ready-to-accept-orders status
     * needs to be updated.
     */
    @Schema(
            description = "Unique ID of the driver whose ready-to-accept-orders status needs to be updated",
            example = "63",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer driverId;

    /**
     * New ready-to-accept-orders value.
     *
     * true  -> Driver is ready to accept orders
     * false -> Driver is not ready to accept orders
     */
    @Schema(
            description = "Indicates whether the driver is ready to accept orders." +
                    " Set true to enable and false to disable",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean readyToAcceptOrders;
}