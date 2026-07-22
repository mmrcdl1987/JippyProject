package com.jippy.division.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class DivScheduledOrderDto {

    @NotNull(message = "Delivery date is required")
    private LocalDate deliveryDate;

    @NotNull(message = "Scheduled delivery time is required")
    private LocalTime scheduledDeliveryTime;

    @NotEmpty(message = "Items cannot be empty")
    private List<DivOrderItemsDto> items;
}
