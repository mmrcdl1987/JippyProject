package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadDocumentsRequestDto {

    @NotBlank(message = "Entity Id is required")
    private Integer entityId;

    @NotBlank(message = "Entity Type is required")
    private String entityType;

    private MultipartFile aadharFile;
    private MultipartFile panFile;
    private MultipartFile fssaiFile;
    private MultipartFile gstFile;
    private MultipartFile rcCopyFile;
    private MultipartFile drivingLicenseFile;
}
