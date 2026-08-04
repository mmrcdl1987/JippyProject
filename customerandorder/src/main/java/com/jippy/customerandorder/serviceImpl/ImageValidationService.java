package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.exception.ImageValidationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
public class ImageValidationService {

    public void validateImage(MultipartFile file) throws IOException {
        // 1. Check if file is empty
        if (file.isEmpty()) {
            throw new ImageValidationException("File cannot be empty");
        }

        // 2. Validate File Type (MIME Type)
        String contentType = file.getContentType();
        if (!isValidType(contentType)) {
            throw new ImageValidationException("Only PNG, JPEG, and JPG are allowed");
        }

        // 3. Validate File Size (e.g., Max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB in bytes
        if (file.getSize() > maxSize) {
            throw new ImageValidationException("File size exceeds the 5MB limit");
        }

        //4. Validate Image Dimensions (e.g., Min 200x200 pixels)
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new ImageValidationException("Invalid image file");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        if (width < 200 || height < 200) {
            throw new ImageValidationException("Image must be at least 200x200 pixels");
        }
    }

    private boolean isValidType(String contentType) {
        return contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"));
    }
}
