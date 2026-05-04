package com.gameplatform.jointpurchaseservice.controller;

import com.gameplatform.jointpurchaseservice.media.dto.response.ImageUploadResponse;
import com.gameplatform.jointpurchaseservice.media.model.StoredImage;
import com.gameplatform.jointpurchaseservice.media.service.MediaStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/joint-purchases/media")
@RequiredArgsConstructor
public class JointPurchaseMediaController {

    private final MediaStorageService mediaStorageService;

    @PostMapping(
            path = "/offer-screenshots",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ImageUploadResponse> uploadOfferScreenshot(
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(toResponse(mediaStorageService.uploadOfferScreenshot(file)));
    }

    @PostMapping(
            path = "/application-screenshots",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ImageUploadResponse> uploadApplicationScreenshot(
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(toResponse(mediaStorageService.uploadApplicationScreenshot(file)));
    }

    private ImageUploadResponse toResponse(StoredImage storedImage) {
        return ImageUploadResponse.builder()
                .bucket(storedImage.bucket())
                .objectKey(storedImage.objectKey())
                .url(storedImage.url())
                .build();
    }
}
