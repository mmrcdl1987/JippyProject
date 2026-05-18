package com.jippy.customerandorder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoCustomerUpdateRequestDto {

    @NotBlank
    private String firstName;

    private String lastName;

    @Email
    private String email;

    private String phoneNumber;

    private Integer updatedBy;
}
