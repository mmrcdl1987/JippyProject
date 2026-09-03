package com.jippy.driver.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class S3ImageService {

    // 2. Pass the S3Client (the tool) to the method that will use it
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3ImageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file, Integer userId, String userType) throws IOException {

        String originalFileName = file.getOriginalFilename();
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

//        String fileName = userType + "/" + userId + "/" +
//        userType.toLowerCase()+userId+extension;


// Generate unique filename using current date and time
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));


        String fileName =
                userType + "/" +
                        userId + "/" +
                        userType.toLowerCase() +
                        "_" +
                        timestamp +
                        extension;
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return fileName;
    }

    // ================================================================
    // DELETE FILE FROM S3
    // ================================================================
    //
    // Deletes an existing profile picture from the S3 bucket.
    //
    // Example:
    //
    // s3Key:
    // DRIVER/63/driver_20260903_113000123.jpg
    //
    // ================================================================

    public void deleteFile(String s3Key) {

        if (s3Key == null || s3Key.trim().isEmpty()) {

            log.info(
                    "[S3] No S3 key provided. Skipping file deletion."
            );

            return;
        }

        try {

            log.info(
                    "[S3] Deleting file from S3. key={}",
                    s3Key
            );

            DeleteObjectRequest deleteObjectRequest =
                    DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Key)
                            .build();

            s3Client.deleteObject(
                    deleteObjectRequest
            );

            log.info(
                    "[S3] File deleted successfully from S3. key={}",
                    s3Key
            );

        } catch (Exception e) {

            log.error(
                    "[S3] Failed to delete file from S3. key={}",
                    s3Key,
                    e
            );

            throw new RuntimeException(
                    "Failed to delete old profile picture from S3",
                    e
            );
        }
    }
}
