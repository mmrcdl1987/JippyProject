package com.jippy.driver.serviceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.IOException;
import java.time.Instant;

@Service
public class S3ImageService {

    // 2. Pass the S3Client (the tool) to the method that will use it
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public S3ImageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file, Integer userId, String userType) throws IOException {

        String originalFileName = file.getOriginalFilename();
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = userType + "/" + userId + "/" +  userType.toLowerCase()+userId+extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return fileName;
    }

    public String uploadDriverDocument(MultipartFile file, Integer driverId, String documentType) throws IOException {
        return uploadDriverDocumentInternal(file, driverId, documentType);
    }

    public String replaceDriverDocument(MultipartFile file, Integer driverId, String documentType,
                                        String oldFileUrl) throws IOException {
        String newFileUrl = uploadDriverDocumentInternal(file, driverId, documentType);
        if (oldFileUrl != null && !oldFileUrl.isBlank() && !oldFileUrl.equals(newFileUrl)) {
            String prefix = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
            if (oldFileUrl.startsWith(prefix)) {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(oldFileUrl.substring(prefix.length()))
                        .build());
            }
        }
        return newFileUrl;
    }

    private String uploadDriverDocumentInternal(MultipartFile file, Integer driverId,
                                                 String documentType) throws IOException {

        String originalFileName = file.getOriginalFilename();
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        long timestamp = Instant.now().toEpochMilli();
        String fileName = "Documents/driver-" + driverId + "/" + documentType + "-" + timestamp + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;
    }
}
