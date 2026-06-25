package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FmCreateMasterProductRequestDto {

    @NotNull(message = "Category Id is required")
    private Integer categoryId;

    @NotBlank(message = "Master Product Name is required")
    private String masterProductName;

    private String description;

    private String shortDescription;

    @NotBlank(message = "Photo is required")
    private String photo;

    private String photos;

    private String thumbnail;

    @NotNull(message = "Veg flag is required")
    private Boolean isVeg;

    private String foodType;

    private String cuisineType;
}