package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequestDto {

    private Integer reviewsId;

    @NotNull(message = "reviewId is required")
    private Integer reviewId;

    @NotNull(message = "customerId is required")
    private Integer customerId;

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "rating must be between 1 and 5")
    @Max(value = 5, message = "rating must be between 1 and 5")
    private Integer rating;

    @Size(max = 500, message = "reviewText must not exceed 500 characters")
    private String reviewText;

    @NotBlank(message = "reviewType is required")
    private String reviewType;

    @NotNull(message = "createdBy is required")
    private Integer createdBy;
}