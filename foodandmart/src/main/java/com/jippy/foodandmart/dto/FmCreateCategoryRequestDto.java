package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FmCreateCategoryRequestDto {

    @NotBlank(message = "Category name is required")
    private String categoryName;
}