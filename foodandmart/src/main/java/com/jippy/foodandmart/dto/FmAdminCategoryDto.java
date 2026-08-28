package com.jippy.foodandmart.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FmAdminCategoryDto {

    private Integer categoryId;

    private String categoryName;

    /**
     * Category availability status.
     */
    private Boolean isAvailable;

    /**
     * Category toggle status.
     */
    private Boolean isToggle;

    private List<FmAdminProductDto> products = new ArrayList<>();
}