package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FmAddressRequestDto {

    private Integer addressId;

    @NotNull(message = "Jippy Address Id is required")
    private Integer jippyAddressId;

    @NotBlank(message = "Address type is required")
    @Pattern(
            regexp = "^(OUTLET|DRIVER|MERCHANT)$",
            message = "Address type must be one of: OUTLET, DRIVER, MERCHANT"
    )
    private String addressType;

    @NotBlank(message = "Building number is required")
    @Size(max = 50, message = "Building number must not exceed 50 characters")
    @Pattern(
            regexp = "^(?!null$).+",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Building number cannot be 'null'"
    )
    private String buildingNumber;

    @NotBlank(message = "Road is required")
    @Size(max = 100, message = "Road must not exceed 100 characters")
    @Pattern(
            regexp = "^(?!null$).+",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Road cannot be 'null'"
    )
    private String road;

    @NotBlank(message = "Landmark is required")
    @Size(max = 150, message = "Landmark must not exceed 150 characters")
    @Pattern(
            regexp = "^(?!null$).+",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Landmark cannot be 'null'"
    )
    private String landmark;

    @NotNull(message = "City Id is required")
    private Integer cityId;

    @NotNull(message = "State Id is required")
    private Integer stateId;

    @NotNull(message = "Area Id is required")
    private Integer areaId;
}