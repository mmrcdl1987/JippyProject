package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "PublicCustomerNearbyResponse", description = "Public nearby outlet response with minimal fields")
public class FmPublicCustomerNearbyResponseDto {

    @Schema(description = "Customer latitude used for the search", example = "17.4484")
    private Double customerLat;

    @Schema(description = "Customer longitude used for the search", example = "78.3799")
    private Double customerLng;

    @Schema(description = "Search radius applied in km", example = "5.0")
    private Double radiusKm;

    @Schema(description = "Total number of outlets found within the radius")
    private Integer totalOutlets;

    @Schema(description = "Response message", example = "Nearby outlets fetched successfully")
    private String message;

    @Schema(description = "Outlets sorted nearest-first")
    private List<FmPublicCustomerNearbyOutletDto> outlets;
}
