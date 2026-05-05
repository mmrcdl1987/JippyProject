package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//data for client/controller
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FmAreaDto {

    private Integer areaId;

    @NotBlank(message = "Area name cannot be empty or only spaces")
    @Size(max = 50, message = "Area name must be less than 50 characters")
    @Pattern(
            regexp = "^[A-Za-z]+( [A-Za-z]+)*$",
            message = "Area name must contain only letters and single spaces between words"
    )
    private String areaName;

    private Integer cityId;

    private LocalDateTime createdAt;
    private Integer createdBy;
    private LocalDateTime updatedAt;
    private Integer updatedBy;
}