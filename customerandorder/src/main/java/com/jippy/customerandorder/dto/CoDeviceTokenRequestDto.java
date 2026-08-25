package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoDeviceTokenRequestDto {

    private Integer userId;

    private String userType;

    private String deviceType;

    private String fcmToken;
}
