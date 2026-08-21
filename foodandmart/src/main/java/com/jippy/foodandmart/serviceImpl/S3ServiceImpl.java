package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.exception.BadRequestException;
import com.jippy.foodandmart.exception.ProductContentException;
import com.jippy.foodandmart.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.product-content-file}")
    private String productContentFile;

    @Value("${aws.s3.region}")
    private String region;

    // PRODUCT CONTENT EXCEL
    @Override
    public InputStream downloadProductContentExcel() {

        log.info(
                "Downloading product content file. bucket={}, key={}",
                bucketName,
                productContentFile
        );

        try {

            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(productContentFile)
                    .build();

            ResponseInputStream<GetObjectResponse> inputStream =
                    s3Client.getObject(request);

            log.info(
                    "Product content Excel downloaded successfully. key={}",
                    productContentFile
            );

            return inputStream;

        } catch (Exception ex) {

            log.error(
                    "Failed to download product content Excel. bucket={}, key={}",
                    bucketName,
                    productContentFile,
                    ex
            );

            throw new ProductContentException(
                    "Unable to download product content Excel from S3.",
                    ex
            );
        }
    }


    // OUTLET IMAGE UPLOAD


    @Override
    public String uploadOutletImage(
            MultipartFile image,
            Integer merchantId) {

        validateOutletImage(image);
        validateMerchantId(merchantId);

        String contentType =
                image.getContentType().toLowerCase();

        String extension =
                IMAGE_EXTENSIONS.get(contentType);

        String objectKey = String.format(
                "outlets/%d/images/%s%s",
                merchantId,
                UUID.randomUUID(),
                extension
        );

        log.info(
                "Uploading outlet image. merchantId={}, size={}, contentType={}, objectKey={}",
                merchantId,
                image.getSize(),
                contentType,
                objectKey
        );

        try (InputStream inputStream = image.getInputStream()) {

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength(image.getSize())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            inputStream,
                            image.getSize()
                    )
            );

            String imageUrl = buildS3Url(objectKey);

            log.info(
                    "Outlet image uploaded successfully. merchantId={}, objectKey={}",
                    merchantId,
                    objectKey
            );

            return imageUrl;

        } catch (IOException ex) {

            log.error(
                    "Unable to read outlet image. merchantId={}, objectKey={}",
                    merchantId,
                    objectKey,
                    ex
            );

            throw new BadRequestException(
                    "Unable to read outlet image."
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to upload outlet image. merchantId={}, objectKey={}",
                    merchantId,
                    objectKey,
                    ex
            );

            throw new BadRequestException(
                    "Unable to upload outlet image."
            );
        }
    }

    // DELETE S3 FILE

    @Override
    public void deleteFile(String fileUrl) {

        if (!StringUtils.hasText(fileUrl)) {
            return;
        }

        try {

            String objectKey = extractObjectKey(fileUrl);

            if (!StringUtils.hasText(objectKey)) {

                log.warn(
                        "Unable to extract S3 object key from URL."
                );

                return;
            }

            DeleteObjectRequest request =
                    DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build();

            s3Client.deleteObject(request);

            log.info(
                    "S3 file deleted successfully. objectKey={}",
                    objectKey
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to delete S3 file.",
                    ex
            );
        }
    }

    // IMAGE VALIDATION

    private void validateOutletImage(MultipartFile image) {

        if (image == null || image.isEmpty()) {

            throw new BadRequestException(
                    "Outlet image is required."
            );
        }

        if (image.getSize() > MAX_IMAGE_SIZE) {

            throw new BadRequestException(
                    "Outlet image must not exceed 5 MB."
            );
        }

        String contentType = image.getContentType();

        if (!StringUtils.hasText(contentType)) {

            throw new BadRequestException(
                    "Outlet image content type is required."
            );
        }

        contentType = contentType.toLowerCase();

        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {

            throw new BadRequestException(
                    "Only JPG, JPEG, PNG and WEBP images are allowed."
            );
        }
    }
    // MERCHANT ID VALIDATION
    private void validateMerchantId(Integer merchantId) {

        if (merchantId == null || merchantId <= 0) {

            throw new BadRequestException(
                    "Valid merchant ID is required."
            );
        }
    }

    // S3 URL

    private String buildS3Url(String objectKey) {

        return String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                region,
                objectKey
        );
    }


    // EXTRACT OBJECT KEY

    private String extractObjectKey(String fileUrl) {

        String prefix = String.format(
                "https://%s.s3.%s.amazonaws.com/",
                bucketName,
                region
        );

        if (!fileUrl.startsWith(prefix)) {
            return null;
        }

        return fileUrl.substring(prefix.length());
    }
}