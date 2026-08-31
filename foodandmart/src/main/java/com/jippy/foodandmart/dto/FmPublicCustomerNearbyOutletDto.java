package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "PublicCustomerNearbyOutlet", description = "Minimal outlet details returned for the public nearby search")
public class FmPublicCustomerNearbyOutletDto {

    @Schema(example = "101")
    private Integer outletId;

    @Schema(example = "Jippy Kitchen - Madhapur")
    private String outletName;

    @Schema(example = "45")
    private Integer merchantId;

    @Schema(example = "4.5")
    private BigDecimal rating;

    @Schema(example = "true")
    private Boolean isActive;

    @Schema(example = "true")
    private Boolean isApproved;

    @Schema(example = "true")
    private Boolean openNow;

    @Schema(example = "true")
    private Boolean isVegOutlet;

    @Schema(example = "https://cdn.example.com/outlet.png")
    private String outletPicUrl;
}
