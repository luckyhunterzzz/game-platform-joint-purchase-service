package com.gameplatform.jointpurchaseservice.media.validation;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageUploadValidator {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Image file size must not exceed 5 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isSupportedContentType(contentType)) {
            throw new IllegalArgumentException("Only PNG, JPEG and WEBP images are allowed");
        }
    }

    private boolean isSupportedContentType(String contentType) {
        return "image/png".equals(contentType)
                || "image/jpeg".equals(contentType)
                || "image/webp".equals(contentType);
    }
}
