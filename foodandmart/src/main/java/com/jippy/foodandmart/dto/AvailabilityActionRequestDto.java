package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AvailabilityActionRequestDto {

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "Unavailability id is required")
    private Integer unavailabilityId;

    private String reason;


}