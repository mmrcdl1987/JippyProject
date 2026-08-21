package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FmCuisineTypeRequestDTO {

    @Schema(
            description = "Cuisine type name",
            example = "INDIAN"
    )
    @NotBlank(message = "Cuisine type name is required")
    @Size(
            max = 100,
            message = "Cuisine type name must not exceed 100 characters"
    )
    private String cuisineTypesName;

    @Schema(
            description = "User who created/updated the cuisine type",
            example = "101"
    )
    private Integer userId;
}