package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FmMerchantDto {

    private Integer merchantId;

    @NotEmpty(message = "Merchant name should not be empty")
    private String merchantName;

    @NotEmpty(message = "Merchant email should not be empty")
    @Email(message = "Invalid email format")
    private String merchantEmail;

    @NotEmpty(message = "Merchant phone should not be empty")
    private String merchantPhone;

    @NotEmpty(message = "Business type should not be empty")
    private String merchantBusinessType;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private Integer createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Integer updatedBy;

    private String isActive;

    private Boolean isApproved;

}
