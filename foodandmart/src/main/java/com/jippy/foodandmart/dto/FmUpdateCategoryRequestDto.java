package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FmUpdateCategoryRequestDto {

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be greater than zero")
    private Integer categoryId;

    @NotBlank(message = "Category name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Category name must be between 2 and 100 characters"
    )
    private String categoryName;

    @Size(
            max = 30,
            message = "Category type cannot exceed 30 characters"
    )
    private String categoryType;

    @Positive(message = "Updated by must be greater than zero")
    private Integer updatedBy;

    /*
     * Optional.
     *
     * If provided:
     *  - Image will be validated
     *  - Uploaded to AWS S3
     *  - S3 URL will be saved in category_image_url
     *
     * If not provided:
     *  - Existing category image will remain unchanged
     */
    private MultipartFile categoryImage;
}