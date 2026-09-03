package com.jippy.foodandmart.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface S3Service {

    InputStream downloadProductContentExcel();

    String uploadOutletImage(
            MultipartFile image,
            Integer merchantId
    );

    String uploadCategoryImage(
            MultipartFile image,
            Integer categoryId
    );

    String uploadKycDocument(MultipartFile document, String userType, Integer userId, String documentType);

    String replaceKycDocument(MultipartFile document, String userType, Integer userId,
                              String documentType, String oldFileUrl);

    void deleteFile(String fileUrl);
}