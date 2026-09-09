package com.jippy.driver.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadProfilePicDto {

    // ------------------------------------------------------------
    // ID of the user whose profile picture is being uploaded.
    //
    // DRIVER   -> driverId
    // CUSTOMER -> customerId
    // MERCHANT -> merchantId
    // OUTLET   -> outletId
    // ------------------------------------------------------------
    private Integer userId;

    // ------------------------------------------------------------
    // Existing profile picture URL.
    // This can be used when returning/updating profile information.
    // ------------------------------------------------------------
    private String profilePicUrl;

    // ------------------------------------------------------------
    // New profile picture uploaded by the user.
    // ------------------------------------------------------------
    private MultipartFile profilePicFile;

    // ------------------------------------------------------------
    // Identifies which type of user is uploading the picture.
    //
    // DRIVER
    // CUSTOMER
    // MERCHANT
    // OUTLET
    // ------------------------------------------------------------
    private String userType;
}