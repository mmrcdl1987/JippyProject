package com.jippy.driver.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminDriverDto {

    private Integer driverId;

    private String firstName;

    private String lastName;

    private String driverName;

    private String phoneNumber;

    private String email;

    private Boolean isApproved;

    private Boolean readyToAcceptOrders;

    private String profilePicUrl;

    private Integer areaId;

    private String areaName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
