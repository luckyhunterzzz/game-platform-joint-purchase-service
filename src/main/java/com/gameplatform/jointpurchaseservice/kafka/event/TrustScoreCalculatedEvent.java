package com.gameplatform.jointpurchaseservice.kafka.event;

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
public class TrustScoreCalculatedEvent {
    private UUID eventId;
    private OffsetDateTime occurredAt;
    private UUID applicationId;
    private UUID offerId;
    private UUID userId;
    private Double score;
    private String riskLevel;
    private String recommendation;
    private UUID sourceEventId;
}