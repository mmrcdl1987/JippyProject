package com.jippy.driver.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DriverIncentiveSettingsDto {

    private Integer driverIncentiveSettingsId;

    @NotNull(message = "Orders count is required")
    @Min(value = 1, message = "Orders count must be at least 1")
    private Integer ordersCount;

    @NotNull(message = "Incentive amount is required")
    @DecimalMin(value = "0.01", inclusive = false, message = "Incentive amount must be greater than 0")
    private BigDecimal incentiveAmount;


}