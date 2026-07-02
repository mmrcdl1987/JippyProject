package com.jippy.customerandorder.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CoCustomerListDto {

    private Integer customerId;
    private String customerName;
    private String email;
    private String phoneNumber;
    private Integer areaId;
    private String areaName;
    private LocalDateTime createdAt;
    private Integer currentStreak;
}