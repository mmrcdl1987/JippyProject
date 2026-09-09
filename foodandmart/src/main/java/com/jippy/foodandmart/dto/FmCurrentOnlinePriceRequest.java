package com.jippy.foodandmart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FmCurrentOnlinePriceRequest {

    @NotNull(message = "Outlet id is required")
    private Integer outletId;

    @Valid
    @NotEmpty(message = "Items are required")
    private List<FmCurrentOnlinePriceItemRequest> items;
}