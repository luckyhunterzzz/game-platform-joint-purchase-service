package com.gameplatform.jointpurchaseservice.dto.response;

import com.gameplatform.jointpurchaseservice.domain.enums.ParticipantFeedbackResult;
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
public class ParticipantFeedbackResponseDto {
    private UUID id;
    private UUID offerId;
    private UUID applicationId;
    private UUID participantUserId;
    private UUID authorUserId;
    private ParticipantFeedbackResult result;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
