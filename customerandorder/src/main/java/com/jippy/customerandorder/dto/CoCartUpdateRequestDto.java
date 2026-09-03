package com.jippy.customerandorder.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CoCartUpdateRequestDto {

    @NotNull(message = "Customer id is required")
    @Min(value = 1, message = "Customer id must be greater than zero")
    private Integer customerId;

    @NotNull(message = "Outlet id is required")
    @Min(value = 1, message = "Outlet id must be greater than zero")
    private Integer outletId;

    @NotNull(message = "Product id is required")
    @Min(value = 1, message = "Product id must be greater than zero")
    private Integer productId;

    @NotEmpty(message = "At least one variant is required")
    @Valid
    private List<CoCartVariantDto> variants;
}