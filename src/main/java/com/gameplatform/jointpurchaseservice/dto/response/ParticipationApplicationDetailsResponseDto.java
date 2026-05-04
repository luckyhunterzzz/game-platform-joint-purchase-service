package com.gameplatform.jointpurchaseservice.dto.response;

import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationApplicationStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationApplicationDetailsResponseDto {
    private UUID id;
    private UUID offerId;
    private UUID applicantUserId;
    private ParticipationApplicationStatus status;
    private ParticipationType assignedParticipationType;
    private UUID reviewedByUserId;
    private OffsetDateTime reviewedAt;
    private String screenshotBucket;
    private String screenshotObjectKey;
    private String screenshotUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private PlayerProfileDetailsResponseDto playerProfile;
}
