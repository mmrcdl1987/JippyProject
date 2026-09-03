package com.jippy.driver.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DriverMerchantDto {

    @JsonProperty("merchantId")
    private Integer merchantId;

    private String merchantName;

    private String merchantEmail;

    private String merchantPhone;

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

    // Used to receive/send the S3 profile picture URL
    private String profilePicUrl;
}