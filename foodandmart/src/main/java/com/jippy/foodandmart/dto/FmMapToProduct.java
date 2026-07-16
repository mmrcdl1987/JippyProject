package com.jippy.foodandmart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FmMapToProduct {

    @NotNull(message = "Outlet Id is required")
    private Integer outletId;

    @NotNull(message = "Category Id is required")
    private Integer categoryId;

    @NotEmpty(message = "Products are required")
    @Valid
    private List<ProductEntry> products;
}