package com.gameplatform.jointpurchaseservice.media.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {

    private String bucket;
    private String objectKey;
    private String url;
}
