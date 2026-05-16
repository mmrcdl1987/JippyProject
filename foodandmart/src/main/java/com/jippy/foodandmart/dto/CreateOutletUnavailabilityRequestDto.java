package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateOutletUnavailabilityRequestDto {

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "Unavailability id is required")
    private Integer unavailabilityId;

    private LocalDateTime unavailabilityFromDate;


    private LocalDateTime unavailabilityToDate;

    @Size(max = 500, message = "Reason max length is 500")
    private String reason;
}