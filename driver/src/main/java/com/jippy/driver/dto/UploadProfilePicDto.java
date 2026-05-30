package com.jippy.driver.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadProfilePicDto {

    private Integer userId;
    private String profilePicUrl;
    private MultipartFile profilePicFile;
    private String userType;
}
