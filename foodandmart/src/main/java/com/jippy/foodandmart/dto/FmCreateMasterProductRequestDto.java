package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FmCreateMasterProductRequestDto {

    @Schema(
            description = "Existing Category Id",
            example = "5"
    )
    @NotNull(message = "Category Id is required")
    @Positive(message = "Category Id must be greater than 0")
    private Integer categoryId;

    @Schema(
            description = "Master Product Name",
            example = "Chicken Biryani"
    )
    @NotBlank(message = "Master Product Name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Master Product Name must be between 2 and 100 characters"
    )
    private String masterProductName;

    @Schema(
            description = "Product Description",
            example = "Delicious Hyderabadi Chicken Biryani"
    )
    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    private String description;

    @Schema(
            description = "Short Description",
            example = "Spicy Chicken Biryani"
    )
    @Size(
            max = 250,
            message = "Short Description cannot exceed 250 characters"
    )
    private String shortDescription;

    @Schema(
            description = "Main Product Image URL",
            example = "https://cdn.jippymart.in/images/chicken-biryani.jpg"
    )
    @NotBlank(message = "Photo is required")
    @Size(
            max = 500,
            message = "Photo URL cannot exceed 500 characters"
    )
    private String photo;

    @Schema(
            description = "Additional Product Images",
            example = "[\"img1.jpg\",\"img2.jpg\"]"
    )
    @Size(
            max = 2000,
            message = "Photos cannot exceed 2000 characters"
    )
    private String photos;

    @Schema(
            description = "Thumbnail Image URL",
            example = "https://cdn.jippymart.in/images/thumb.jpg"
    )
    @Size(
            max = 500,
            message = "Thumbnail URL cannot exceed 500 characters"
    )
    private String thumbnail;

    @Schema(
            description = "Veg Product",
            example = "true"
    )
    @NotNull(message = "Veg flag is required")
    private Boolean isVeg;

    @Schema(
            description = "Food Type",
            example = "VEG",
            allowableValues = {
                    "VEG",
                    "NON_VEG",
                    "EGG"
            }
    )
    @Pattern(
            regexp = "^(VEG|NON_VEG|EGG)?$",
            message = "Food Type must be VEG, NON_VEG or EGG"
    )
    private String foodType;

    @Schema(
            description = "Cuisine Type",
            example = "Indian"
    )
    @Pattern(
            regexp = "^[A-Za-z ]*$",
            message = "Cuisine Type can contain only alphabets and spaces"
    )
    @Size(
            max = 100,
            message = "Cuisine Type cannot exceed 100 characters"
    )
    private String cuisineType;
}