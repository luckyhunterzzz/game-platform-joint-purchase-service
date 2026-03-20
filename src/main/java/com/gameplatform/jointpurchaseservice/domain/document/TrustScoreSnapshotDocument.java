package com.gameplatform.jointpurchaseservice.domain.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trust_score_snapshots")
public class TrustScoreSnapshotDocument {

    @Id
    private String id;

    private UUID applicationId;
    private UUID offerId;
    private UUID userId;

    private Double score;
    private String riskLevel;
    private String recommendation;

    private UUID trustScoreEventId;
    private UUID sourceEventId;

    private OffsetDateTime calculatedAt;
    private OffsetDateTime receivedAt;
}