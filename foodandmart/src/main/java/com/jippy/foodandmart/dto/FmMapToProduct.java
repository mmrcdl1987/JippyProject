package com.jippy.foodandmart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FmMapToProduct {

    /**
     * Existing mobile application can send this value.
     *
     * If supplied, it is used as the outlet category.
     *
     * Bulk upload can leave this null because products
     * may belong to different master-product categories.
     */
    private Integer outletCategoryId;

    /**
     * Outlet where products have to be added.
     */
    @NotNull(message = "Outlet Id is required")
    private Integer outletId;

    /**
     * Optional for backward compatibility.
     *
     * For bulk upload, every ProductEntry can have its own
     * categoryId and the backend will resolve the correct
     * outlet_category_id.
     */
    private Integer categoryId;

    /**
     * One or many master products.
     */
    @NotEmpty(message = "Products are required")
    @Valid
    private List<ProductEntry> products;
}