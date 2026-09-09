package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CustomerProfilePicDto {

    // Customer ID whose profile picture is being updated
    private Integer customerId;

    // S3 URL of the new profile picture
    private String profilePicUrl;
}