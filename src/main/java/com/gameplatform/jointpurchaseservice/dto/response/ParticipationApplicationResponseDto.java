package com.gameplatform.jointpurchaseservice.dto.response;

import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationApplicationStatus;
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
public class ParticipationApplicationResponseDto {
    private UUID id;
    private UUID offerId;
    private UUID applicantUserId;
    private ParticipationApplicationStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}