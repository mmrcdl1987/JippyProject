package com.jippy.customerandorder.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OtpCacheDto implements Serializable {

    private Long customerId;

    private String otpHash;

    private Integer retryCount;

    private Integer resendCount;
}