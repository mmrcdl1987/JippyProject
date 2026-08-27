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

    void deleteFile(String fileUrl);
}