package com.jippy.customerandorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoSaveWalletPointsRequestDTO {

    @NotBlank(message = "Order ID is required")
    @Size(
            max = 50,
            message = "Order ID must not exceed 50 characters"
    )
    @Schema(
            example = "ORD012",
            description = "Order ID for which wallet points are being saved",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String orderId;
}