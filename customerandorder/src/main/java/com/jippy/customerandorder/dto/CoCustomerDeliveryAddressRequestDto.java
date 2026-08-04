package com.jippy.customerandorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CoCustomerDeliveryAddressRequestDto {

    @Schema(description = "Unique customer ID", example = "104",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Customer Id is required")
    @Positive(message = "Customer Id must be greater than zero")
    private Integer customerId;

    @Schema(description = "Latitude of the customer delivery location", example = "17.385044",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
    @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
    @NotNull(message = "Latitude is required")
    private Double latitude;

    @Schema(description = "Longitude of the customer delivery location", example = "78.486671",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180")
    @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180")
    @NotNull(message = "Longitude is required")
    private Double longitude;

    @Schema(description = "Door number of the delivery address", example = "12-4-567",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Door No is required")
    private String doorNo;

    @Schema(description = "Building or apartment name", example = "My Home Apartments",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Building Name is required")
    private String buildingName;

    @Schema(description = "Lane number or street name", example = "Lane 5",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Lane No is required")
    private String laneNo;

    @Schema(description = "Area ID (Optional)", example = "15")
    private Integer area;

    @Schema(description = "City ID (Optional)", example = "3")
    private Integer city;

    @Schema(description = "User ID who created the address", example = "1")
    @NotNull(message = "Created By is required")
    @Positive(message = "Created By must be greater than zero")
    private Integer createdBy;
}