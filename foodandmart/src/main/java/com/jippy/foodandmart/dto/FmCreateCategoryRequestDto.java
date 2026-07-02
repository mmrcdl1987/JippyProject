package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FmCreateCategoryRequestDto {


    @Schema(description = "Name of the category",
            example = "Pizza",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Category name is required")
    private String categoryName;

    @Schema(description = "Category type. Allowed values are ALL or HOME",
            example = "ALL",
            allowableValues = {"ALL", "HOME"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Category type is required")
    @Pattern(regexp = "^(ALL|HOME)$", message = "Category type must be either ALL or HOME")
    private String categoryType;

    @Schema(description = "URL of the category image",
            example = "https://jippy-images.s3.ap-south-1.amazonaws.com/categories/pizza.png")
    private String categoryImageUrl;

    private Integer createdBy;
}