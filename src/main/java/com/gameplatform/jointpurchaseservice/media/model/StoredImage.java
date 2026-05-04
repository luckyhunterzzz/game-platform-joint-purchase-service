package com.gameplatform.jointpurchaseservice.media.model;

public record StoredImage(
        String bucket,
        String objectKey,
        String url
) {
}
