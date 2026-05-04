package com.gameplatform.jointpurchaseservice.dto.request;

import jakarta.validation.constraints.Size;
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
public class SubmitParticipationApplicationRequestDto {

    private String screenshotBucket;

    @Size(max = 1024)
    private String screenshotObjectKey;
}
