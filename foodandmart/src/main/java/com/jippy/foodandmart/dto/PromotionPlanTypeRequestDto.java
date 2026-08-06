package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PromotionPlanTypeRequestDto {

    @NotBlank(message = "Plan name is required")
    @Size(min = 2, max = 100, message = "Plan name must be between 2 and 100 characters")
    @Pattern(
            regexp = "^(?!\\s*$)[A-Za-z0-9 &()'.,/-]+$",
            message = "Plan name contains invalid characters"
    )
    private String planName;
}