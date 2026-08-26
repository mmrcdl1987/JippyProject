package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmCreateCategoryRequestDto;
import com.jippy.foodandmart.dto.FmCreateCategoryResponseDto;
import com.jippy.foodandmart.dto.FmUpdateCategoryRequestDto;
import com.jippy.foodandmart.entity.FmCategory;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.ImageValidationException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.CategoryMapper;
import com.jippy.foodandmart.repository.FmCategoryRepository;
import com.jippy.foodandmart.service.IFmCategoryService;
import com.jippy.foodandmart.service.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FmCategoryServiceImpl implements IFmCategoryService {

    private final FmCategoryRepository categoryRepository;

    /*
     * Food & Mart AWS S3 service
     */
    private final S3Service s3Service;


    @Override
    @Transactional
    public FmCreateCategoryResponseDto createCategory(FmCreateCategoryRequestDto request) {

        log.info("CREATE_CATEGORY_STARTED | categoryName={}", request.getCategoryName());

        if (categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName().trim())) {

            throw new DuplicateResourceException("Category already exists : " + request.getCategoryName());
        }

        FmCategory category = CategoryMapper.toEntity(request);

        FmCategory savedCategory = categoryRepository.save(category);

        log.info("CREATE_CATEGORY_COMPLETED | categoryId={}", savedCategory.getCategoryId());

        return CategoryMapper.toResponseDto(savedCategory);
    }


    //    ----------------------------------------------------------------------------
    @Override
    public List<FmCreateCategoryResponseDto> getHomeOrAllCategories(String filter) {

        log.info("GET_HOME_OR_ALL_CATEGORIES_STARTED | filter={}", filter);

        List<FmCategory> categoryList;

        if (FmAppConstants.CATEGORY_TYPE_ALL.equalsIgnoreCase(filter)) {

            log.info("Fetching all categories");

            categoryList = categoryRepository.findAll();

        } else if (FmAppConstants.CATEGORY_TYPE_HOME.equalsIgnoreCase(filter)) {

            log.info("Fetching HOME categories");

            categoryList = categoryRepository.findByCategoryType(FmAppConstants.CATEGORY_TYPE_HOME);

        } else {

            throw new IllegalArgumentException("Invalid filter. Allowed values are ALL or HOME.");
        }

        List<FmCreateCategoryResponseDto> responseList = new ArrayList<>();

        for (FmCategory category : categoryList) {

            responseList.add(CategoryMapper.toResponseDto(category));
        }

        log.info("GET_HOME_OR_ALL_CATEGORIES_COMPLETED | totalCategories={}", responseList.size());

        return responseList;
    }


    //    ----------------------------------------------------------------------------
    /*
     * UPDATE CATEGORY
     *
     * This API updates:
     *
     * 1. Category name
     * 2. Category type
     * 3. Updated by
     * 4. Category image
     *
     * Category image is optional.
     *
     * If a new image is provided:
     *
     * Request
     *     ↓
     * Validate image
     *     ↓
     * Upload image to S3
     *     ↓
     * Receive complete S3 URL
     *     ↓
     * Update category_image_url
     *     ↓
     * Save category
     *
     * If image is not provided:
     *
     * Existing category_image_url remains unchanged.
     */
    @Override
    @Transactional
    public FmCreateCategoryResponseDto updateCategory(FmUpdateCategoryRequestDto request) {

        log.info("UPDATE_CATEGORY_STARTED | categoryId={}", request != null ? request.getCategoryId() : null);


        /*
         * ============================================================
         * 1. REQUEST VALIDATION
         * ============================================================
         */

        validateUpdateCategoryRequest(request);


        /*
         * ============================================================
         * 2. FIND CATEGORY
         * ============================================================
         */

        FmCategory category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> {

            log.warn("UPDATE_CATEGORY_FAILED | Category not found | categoryId={}", request.getCategoryId());

            return new ResourceNotFoundException("Category not found with id: " + request.getCategoryId());
        });


        /*
         * ============================================================
         * 3. NORMALIZE CATEGORY NAME
         * ============================================================
         */

        String categoryName = request.getCategoryName().trim();


        /*
         * ============================================================
         * 4. DUPLICATE CATEGORY NAME VALIDATION
         * ============================================================
         *
         * Same category name for the same category is allowed.
         *
         * Another category having the same name is not allowed.
         */

        if (!category.getCategoryName().equalsIgnoreCase(categoryName) && categoryRepository.existsByCategoryNameIgnoreCase(categoryName)) {

            log.warn("UPDATE_CATEGORY_FAILED | Duplicate category name | categoryName={}", categoryName);

            throw new DuplicateResourceException("Category already exists : " + categoryName);
        }


        /*
         * ============================================================
         * 5. UPDATE CATEGORY DETAILS
         * ============================================================
         */

        category.setCategoryName(categoryName);


        if (request.getCategoryType() != null && !request.getCategoryType().trim().isEmpty()) {

            category.setCategoryType(request.getCategoryType().trim());
        }


        if (request.getUpdatedBy() != null) {

            category.setUpdatedBy(request.getUpdatedBy());
        }


        /*
         * ============================================================
         * 6. CATEGORY IMAGE UPDATE
         * ============================================================
         *
         * Image is optional.
         */

        MultipartFile categoryImage = request.getCategoryImage();


        if (categoryImage != null && !categoryImage.isEmpty()) {

            log.info("CATEGORY_IMAGE_UPDATE_STARTED | categoryId={}", category.getCategoryId());


            /*
             * Validate category image
             */
            validateCategoryImage(categoryImage);


            /*
             * ========================================================
             * Upload image to AWS S3
             * ========================================================
             *
             * uploadCategoryImage() should return the COMPLETE
             * S3 image URL.
             */

            String categoryImageUrl = s3Service.uploadCategoryImage(categoryImage, category.getCategoryId());


            /*
             * Make sure S3 service returned a valid URL.
             */

            if (categoryImageUrl == null || categoryImageUrl.trim().isEmpty()) {

                log.error("CATEGORY_IMAGE_UPLOAD_FAILED | Empty S3 URL | categoryId={}", category.getCategoryId());

                throw new ImageValidationException("Category image upload failed");
            }


            /*
             * ========================================================
             * Update category image URL
             * ========================================================
             */

            category.setCategoryImageUrl(categoryImageUrl);


            log.info("CATEGORY_IMAGE_URL_UPDATED | categoryId={}", category.getCategoryId());
        }


        /*
         * ============================================================
         * 7. SAVE CATEGORY
         * ============================================================
         */

        FmCategory savedCategory = categoryRepository.save(category);


        log.info("UPDATE_CATEGORY_COMPLETED | categoryId={}", savedCategory.getCategoryId());


        /*
         * ============================================================
         * 8. RETURN RESPONSE
         * ============================================================
         */

        return CategoryMapper.toResponseDto(savedCategory);
    }


    //    ----------------------------------------------------------------------------
    /*
     * Validate update category request.
     */
    private void validateUpdateCategoryRequest(FmUpdateCategoryRequestDto request) {

        /*
         * Request cannot be null
         */
        if (request == null) {

            throw new IllegalArgumentException("Update category request cannot be null");
        }


        /*
         * Category ID validation
         */
        if (request.getCategoryId() == null) {

            throw new IllegalArgumentException("Category ID is required");
        }


        /*
         * Category ID must be positive
         */
        if (request.getCategoryId() <= 0) {

            throw new IllegalArgumentException("Category ID must be greater than zero");
        }


        /*
         * Category name validation
         */
        if (request.getCategoryName() == null || request.getCategoryName().trim().isEmpty()) {

            throw new IllegalArgumentException("Category name is required");
        }


        String categoryName = request.getCategoryName().trim();


        /*
         * Minimum category name length
         */
        if (categoryName.length() < 2) {

            throw new IllegalArgumentException("Category name must contain at least 2 characters");
        }


        /*
         * Maximum category name length
         *
         * Database:
         * category_name VARCHAR(100)
         */
        if (categoryName.length() > 100) {

            throw new IllegalArgumentException("Category name cannot exceed 100 characters");
        }


        /*
         * Prevent control characters
         */
        if (containsControlCharacter(categoryName)) {

            throw new IllegalArgumentException("Category name contains invalid characters");
        }


        /*
         * Category type validation
         *
         * Database:
         * category_type VARCHAR(30)
         */
        if (request.getCategoryType() != null && request.getCategoryType().trim().length() > 30) {

            throw new IllegalArgumentException("Category type cannot exceed 30 characters");
        }
    }


    //    ----------------------------------------------------------------------------
    /*
     * Production-grade category image validation.
     *
     * Validation includes:
     *
     * 1. File existence
     * 2. File size
     * 3. MIME type
     * 4. File extension
     * 5. Actual image content
     * 6. Minimum dimensions
     * 7. Maximum dimensions
     * 8. Aspect ratio
     */
    private void validateCategoryImage(MultipartFile file) {

        /*
         * ============================================================
         * 1. FILE EXISTENCE
         * ============================================================
         */

        if (file == null || file.isEmpty()) {

            throw new ImageValidationException("Category image cannot be empty");
        }


        /*
         * ============================================================
         * 2. FILE SIZE
         * ============================================================
         */

        if (file.getSize() > FmAppConstants.CATEGORY_IMAGE_MAX_SIZE) {

            throw new ImageValidationException("Category image size cannot exceed 5 MB");
        }


        /*
         * ============================================================
         * 3. MIME TYPE
         * ============================================================
         */

        String contentType = file.getContentType();


        if (!isAllowedImageContentType(contentType)) {

            throw new ImageValidationException("Only JPG, JPEG, PNG and WEBP images are allowed");
        }


        /*
         * ============================================================
         * 4. FILE EXTENSION
         * ============================================================
         */

        String originalFileName = file.getOriginalFilename();


        if (!isAllowedImageExtension(originalFileName)) {

            throw new ImageValidationException("Invalid image file extension");
        }


        /*
         * ============================================================
         * 5. ACTUAL IMAGE CONTENT VALIDATION
         * ============================================================
         *
         * This prevents a non-image file from simply being renamed
         * to .jpg, .jpeg, .png or .webp.
         */

        try {

            BufferedImage image = ImageIO.read(file.getInputStream());


            if (image == null) {

                throw new ImageValidationException("Uploaded file is not a valid image");
            }


            /*
             * ========================================================
             * 6. IMAGE DIMENSIONS
             * ========================================================
             */

            int width = image.getWidth();

            int height = image.getHeight();


            /*
             * Minimum dimensions
             */

            if (width < FmAppConstants.CATEGORY_IMAGE_MIN_WIDTH || height < FmAppConstants.CATEGORY_IMAGE_MIN_HEIGHT) {

                throw new ImageValidationException("Image dimensions must be at least 200x200 pixels");
            }


            /*
             * Maximum dimensions
             */

            if (width > FmAppConstants.CATEGORY_IMAGE_MAX_WIDTH || height > FmAppConstants.CATEGORY_IMAGE_MAX_HEIGHT) {

                throw new ImageValidationException("Image dimensions cannot exceed 5000x5000 pixels");
            }


            /*
             * ========================================================
             * 7. ASPECT RATIO
             * ========================================================
             */

            double aspectRatio = (double) width / height;


            if (aspectRatio < FmAppConstants.CATEGORY_IMAGE_MIN_ASPECT_RATIO || aspectRatio > FmAppConstants.CATEGORY_IMAGE_MAX_ASPECT_RATIO) {

                throw new ImageValidationException("Invalid image aspect ratio");
            }


        } catch (IOException e) {

            log.error("CATEGORY_IMAGE_VALIDATION_FAILED", e);

            throw new ImageValidationException("Unable to validate category image", e);
        }
    }


    //    ----------------------------------------------------------------------------
    /*
     * Validate allowed image MIME types.
     */
    private boolean isAllowedImageContentType(String contentType) {

        if (contentType == null) {
            return false;
        }


        return contentType.equalsIgnoreCase(FmAppConstants.IMAGE_CONTENT_TYPE_JPEG) || contentType.equalsIgnoreCase(FmAppConstants.IMAGE_CONTENT_TYPE_PNG) || contentType.equalsIgnoreCase(FmAppConstants.IMAGE_CONTENT_TYPE_WEBP);
    }


    //    ----------------------------------------------------------------------------
    /*
     * Validate allowed image extensions.
     */
    private boolean isAllowedImageExtension(String fileName) {

        if (fileName == null || fileName.isBlank()) {

            return false;
        }


        String lowerCaseFileName = fileName.toLowerCase();


        return lowerCaseFileName.endsWith(FmAppConstants.IMAGE_EXTENSION_JPG) || lowerCaseFileName.endsWith(FmAppConstants.IMAGE_EXTENSION_JPEG) || lowerCaseFileName.endsWith(FmAppConstants.IMAGE_EXTENSION_PNG) || lowerCaseFileName.endsWith(FmAppConstants.IMAGE_EXTENSION_WEBP);
    }


    //    ----------------------------------------------------------------------------
    /*
     * Prevent control characters in category name.
     */
    private boolean containsControlCharacter(String value) {

        for (char character : value.toCharArray()) {

            if (Character.isISOControl(character)) {

                return true;
            }
        }

        return false;
    }

}