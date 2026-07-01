package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
@Data
@Schema(description = "Request DTO for adding or removing a favorite outlet")
public class FmFavoriteOutletRequestDto {

    @NotNull(message = "Customer Id is required")
    @Positive(message = "Customer Id must be greater than 0")
    @Schema(
            example = "101",
            description = "Unique customer identifier",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer customerId;

//    @NotNull(message = "Outlet Id is required")
//    @Positive(message = "Outlet Id must be greater than 0")
//    @Schema(
//            example = "501",
//            description = "Unique outlet identifier",
//            requiredMode = Schema.RequiredMode.REQUIRED
//    )
//    private Integer outletId;

    @Schema(example = "16")
    private Integer favoriteId;

    @Schema(example = "OUTLET",
            allowableValues = {"OUTLET","PRODUCT"},
            description = "Favourite entity type"
    )
    private String favouriteType;

//    @NotNull(message = "Created By is required")
//    @Positive(message = "Created By must be greater than 0")
//    @Schema(
//            example = "101",
//            description = "User who is performing the favorite/unfavorite action",
//            requiredMode = Schema.RequiredMode.REQUIRED
//    )
//    private Integer createdBy;
}
