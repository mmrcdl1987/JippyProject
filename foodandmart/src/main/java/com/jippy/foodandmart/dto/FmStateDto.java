package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FmStateDto {

    private Integer stateId;

    @NotBlank(message = "State name cannot be empty or only spaces")
    @Size(max = 50, message = "State name must be less than 50 characters")
    @Pattern(
            regexp = "^[A-Za-z]+( [A-Za-z]+)*$",
            message = "State name must contain only letters and single spaces between words"
    )
    private String stateName;

    private LocalDateTime createdAt;
    private Integer createdBy;
    private LocalDateTime updatedAt;
    private Integer updatedBy;
}