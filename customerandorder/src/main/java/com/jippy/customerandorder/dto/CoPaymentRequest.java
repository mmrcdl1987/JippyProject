package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CoPaymentRequest {

    @NotBlank(message = "Payment mode is required")
    @Size(max = 50, message = "Payment mode cannot exceed 50 characters")
    private String paymentMode;

    @NotBlank(message = "Active status is required")
    @Size(max = 1, message = "Active status must be Y or N")
    private String isActive;
}