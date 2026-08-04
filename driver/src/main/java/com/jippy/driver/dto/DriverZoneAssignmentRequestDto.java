package com.jippy.driver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DriverZoneAssignmentRequestDto {

    @Schema(description = "Unique identifier of the driver to whom the zone will be assigned", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Driver id is required")
    @Positive(message = "Driver id must be greater than 0 Negative Driver Id's are Not Allowed")
    private Integer driverId;

    @Schema(description = "Latitude of the driver's current location. Provide this together" +
            " with longitude instead of zoneId. The coordinates must match an existing zone.",
            example = "15.914116849775338")
    @DecimalMin(value = "-90.0", message = "Latitude should be greater than or equal to -90")
    @DecimalMax(value = "90.0", message = "Latitude should be less than or equal to 90")
    private Double latitude;

    @Schema(description = "Longitude of the driver's current location. Provide this together with" +
            " latitude instead of zoneId. The coordinates must match an existing zone.",
            example = "80.40316170183388")
    @DecimalMin(value = "-180.0", message = "Longitude should be greater than or equal to -180")
    @DecimalMax(value = "180.0", message = "Longitude should be less than or equal to 180")
    private Double longitude;

    @Schema(description = "Existing zone identifier. Provide this instead of latitude and longitude.", example = "3")
    @Positive(message="Zone id must be greater than 0")
    private Integer zoneId;
}