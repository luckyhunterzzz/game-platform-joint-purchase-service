package com.gameplatform.jointpurchaseservice.media.resolver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Component
public class MediaUrlResolver {

    private final String publicBaseUrl;

    public MediaUrlResolver(@Value("${app.minio.public-base-url}") String publicBaseUrl) {
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
    }

    public String resolvePublicUrl(String bucket, String objectKey) {
        if (bucket == null || bucket.isBlank() || objectKey == null || objectKey.isBlank()) {
            return null;
        }

        String normalizedBucket = encodePathSegment(bucket);
        String normalizedObjectKey = encodeObjectKey(objectKey);

        return publicBaseUrl + "/" + normalizedBucket + "/" + normalizedObjectKey;
    }

    private String encodeObjectKey(String objectKey) {
        return UriUtils.encodePath(objectKey, StandardCharsets.UTF_8);
    }

    private String encodePathSegment(String value) {
        return UriUtils.encodePathSegment(value, StandardCharsets.UTF_8);
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }

        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
