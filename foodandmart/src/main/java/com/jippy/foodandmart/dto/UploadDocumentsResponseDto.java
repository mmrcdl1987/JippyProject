package com.jippy.foodandmart.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadDocumentsResponseDto {

    private Integer entityId;
    private String entityType;
    private String aadharFileUrl;
    private String panFileUrl;
    private String fssaiFileUrl;
    private String gstFileUrl;
    private String rcCopyFileUrl;
    private String drivingLicenseFileUrl;
}
