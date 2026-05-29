package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "OutletsResponse", description = "Paginated list of outlets near the customer")
public class FmOutletsResponseDto {

    @Schema(description = "Customer latitude used for search", example = "17.4484")
    private Double customerLat;

    @Schema(description = "Customer longitude used for search", example = "78.3799")
    private Double customerLng;

    @Schema(description = "Search radius in km", example = "3.0")
    private Double radiusKm;

    @Schema(description = "Total outlets found within radius")
    private Integer totalOutlets;

    @Schema(description = "List of nearby outlets")
    private List<com.jippy.foodandmart.dto.FmOutletDto> outlets;

// these feilds ar used in Co Merchant Settlement API response,
// contains outlet details along with areaId which is used to fetch area details from Area microservice
    private Integer outletId;

    private String outletName;

    private String outletPhone;

    private Integer areaId;

    private String areaName;
}
