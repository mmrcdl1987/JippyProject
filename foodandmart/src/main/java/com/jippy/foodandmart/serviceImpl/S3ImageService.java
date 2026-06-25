package com.jippy.foodandmart.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@Slf4j
public class S3ImageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public S3ImageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadBanner(MultipartFile file, Integer outletId, Integer subscriptionPlanId, String bannerType, Integer slotNumber) throws IOException {
        String op = "S3_UPLOAD_" + UUID.randomUUID();
        String originalFileName = file.getOriginalFilename();
        long fileSize = file.getSize();

        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = "outlet-banners/" + outletId + "/" + subscriptionPlanId + "/" + bannerType + "/slot-" + slotNumber + extension;

        log.info("{} | START | uploading banner | outletId={} | subscriptionPlanId={} | bannerType={} | slot={} | originalFileName={} | size={} bytes", op, outletId, subscriptionPlanId, bannerType, slotNumber, originalFileName, fileSize);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        long start = System.currentTimeMillis();
        try (InputStream is = file.getInputStream()) {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(is, fileSize));
            long took = System.currentTimeMillis() - start;
            String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
            log.info("{} | SUCCESS | uploaded banner | outletId={} | key={} | took={}ms", op, outletId, fileName, took);
            // return the public URL (ensure your bucket policy or CDN exposes this)
            return url;
        } catch (Exception ex) {
            long took = System.currentTimeMillis() - start;
            log.error("{} | ERROR | upload failed | outletId={} | key={} | took={}ms | error={}", op, outletId, fileName, took, ex.getMessage(), ex);
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            throw new IOException("Failed to upload banner to S3", ex);
        }
    }
}