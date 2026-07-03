package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmCreateOutletOtpRequestDto {

    @NotNull(message = "Merchant Id is required")
    private Integer merchantId;

}