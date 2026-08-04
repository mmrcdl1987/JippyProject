package com.jippy.driver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request DTO for creating or updating driver incentive settings")
public class DriverIncentiveSettingsDto {

    @Schema(
            description = "Driver incentive setting ID. Keep null while creating a new incentive " +
                    "and provide the ID while updating.",
            example = "1",
            accessMode = Schema.AccessMode.READ_WRITE
    )
    private Integer driverIncentiveSettingsId;

    @NotNull(message = "Orders count is required")
    @Min(value = 1, message = "Orders count must be at least 1")
    @Schema(description = "Minimum number of completed orders required to earn the incentive.",
            example = "23", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ordersCount;

    @NotNull(message = "Incentive amount is required")
    @DecimalMin(value = "0.01",
            inclusive = false,
            message = "Incentive amount must be greater than 0"
    )
    @Schema(
            description = "Incentive amount awarded to the driver for completing" +
                    " the specified number of orders.",
            example = "250.00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal incentiveAmount;
}