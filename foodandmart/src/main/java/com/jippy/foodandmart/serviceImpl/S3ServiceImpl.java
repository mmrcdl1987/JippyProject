package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.exception.ProductContentException;
import com.jippy.foodandmart.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.product-content-file}")
    private String productContentFile;

    @Override
    public InputStream downloadProductContentExcel() {

        log.info("Downloading file {} from bucket {}",
                productContentFile,
                bucketName);

        try {

            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(productContentFile)
                    .build();

            ResponseInputStream<GetObjectResponse> inputStream =
                    s3Client.getObject(request);

            log.info("Product content Excel downloaded successfully.");

            return inputStream;

        } catch (Exception ex) {

            log.error("Failed to download product content Excel.", ex);

            throw new ProductContentException(
                    "Unable to download product content Excel from S3.",
                    ex
            );
        }

    }

}