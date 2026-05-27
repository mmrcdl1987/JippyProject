package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CustomerNearbyResponse", description = "Response wrapper for Customer App nearby outlet search")
public class FmCustomerNearbyResponseDto {

    @Schema(description = "Customer latitude used for the search", example = "17.4484")
    private Double customerLat;

    @Schema(description = "Customer longitude used for the search", example = "78.3799")
    private Double customerLng;

    @Schema(description = "Search radius applied (km)", example = "3.0")
    private Double radiusKm;

    @Schema(description = "Total number of outlets found within the radius")
    private Integer totalOutlets;

    @Schema(description = "Message when no outlets are available", example = "Service is not available in this area")
    private String message;

    @Schema(description = "Outlets sorted nearest-first")
    private List<com.jippy.division.dto.FmNearbyOutletDto> outlets;
}