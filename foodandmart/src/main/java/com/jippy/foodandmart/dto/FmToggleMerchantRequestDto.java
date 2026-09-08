package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Toggle Merchant Request",
        description = "Request model used to activate or deactivate a merchant")
public class FmToggleMerchantRequestDto {

    @Schema(description = "Unique ID of the merchant", example = "132",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer merchantId;

    @Schema(description = "Merchant active status. Set true to activate and false to deactivate",
            example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isActive;
}
