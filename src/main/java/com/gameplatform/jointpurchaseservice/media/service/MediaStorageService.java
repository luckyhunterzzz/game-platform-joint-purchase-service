package com.gameplatform.jointpurchaseservice.media.service;

import com.gameplatform.jointpurchaseservice.media.model.StoredImage;
import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {

    StoredImage uploadOfferScreenshot(MultipartFile file);

    StoredImage uploadApplicationScreenshot(MultipartFile file);

    String createApplicationScreenshotAccessUrl(String bucket, String objectKey);
}
