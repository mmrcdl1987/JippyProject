package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class CoReorderRequestDto {

    @NotNull(message = "Customer Id is required")
    @Positive(message = "Customer Id must be greater than zero")
    private Integer customerId;

    @NotBlank(message = "Order Id is required")
    @Size(max = 100, message = "Order Id must not exceed 100 characters")
    private String orderId;
}