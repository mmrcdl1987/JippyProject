package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Outlet", description = "Outlet details")
public class FmOutletDto {

    private Integer outletId;
    private String outletName;
    private Integer merchantId;
    private Integer[] cuisineType;
    private String outletPhone;
    private BigDecimal radius;
    private String subscriptionStatus;
    private String promotionStatus;
    private Double review;
    private Boolean isActive;
    private Boolean isApproved;
    private Double distanceKm;

     public FmOutletDto(Integer outletId, String outletName) {
        this.outletId = outletId;
        this.outletName = outletName;
    }

    // ============================================================
    // OUTLET PROFILE PICTURE
    // ============================================================

    @Schema(
            description = "S3 URL of the outlet profile picture",
            example = "https://jippys3bucket.s3.ap-south-2.amazonaws.com/OUTLET/132/outlet_20260902_223015123.jpg"
    )
    private String outletPicUrl;
}
