package com.gameplatform.jointpurchaseservice.service;

import com.gameplatform.jointpurchaseservice.domain.document.TrustScoreSnapshotDocument;
import com.gameplatform.jointpurchaseservice.domain.entity.ParticipationApplication;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationApplicationStatus;
import com.gameplatform.jointpurchaseservice.kafka.event.TrustScoreCalculatedEvent;
import com.gameplatform.jointpurchaseservice.repository.jpa.ParticipationApplicationRepository;
import com.gameplatform.jointpurchaseservice.repository.mongo.TrustScoreSnapshotMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrustScoreProcessingService {

    private final ParticipationApplicationRepository participationApplicationRepository;
    private final TrustScoreSnapshotMongoRepository trustScoreSnapshotMongoRepository;
    private final Clock clock;

    @Transactional
    public void processTrustScoreCalculatedEvent(TrustScoreCalculatedEvent event) {
        ParticipationApplication application = participationApplicationRepository.findById(event.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Participation application not found: " + event.getApplicationId()
                ));

        validateEventMatchesApplication(application, event);

        ParticipationApplicationStatus newStatus = mapRecommendationToStatus(event.getRecommendation());

        OffsetDateTime now = OffsetDateTime.now(clock);

        application.setStatus(newStatus);
        application.setUpdatedAt(now);

        participationApplicationRepository.save(application);

        TrustScoreSnapshotDocument snapshotDocument = TrustScoreSnapshotDocument.builder()
                .applicationId(event.getApplicationId())
                .offerId(event.getOfferId())
                .userId(event.getUserId())
                .score(event.getScore())
                .riskLevel(event.getRiskLevel())
                .recommendation(event.getRecommendation())
                .trustScoreEventId(event.getEventId())
                .sourceEventId(event.getSourceEventId())
                .calculatedAt(event.getOccurredAt())
                .receivedAt(now)
                .build();

        // TODO: Postgres update and Mongo snapshot save are not atomic across different storages.
        // Later consider idempotency and audit decoupling.
        trustScoreSnapshotMongoRepository.save(snapshotDocument);

        log.info(
                "Trust score processed successfully: applicationId={}, userId={}, newStatus={}, score={}, recommendation={}",
                application.getId(),
                application.getApplicantUserId(),
                newStatus,
                event.getScore(),
                event.getRecommendation()
        );
    }

    private void validateEventMatchesApplication(
            ParticipationApplication application,
            TrustScoreCalculatedEvent event
    ) {
        if (!application.getApplicantUserId().equals(event.getUserId())) {
            throw new IllegalArgumentException(
                    "Event userId does not match application applicantUserId. applicationId=" + application.getId()
            );
        }

        if (!application.getOfferId().equals(event.getOfferId())) {
            throw new IllegalArgumentException(
                    "Event offerId does not match application offerId. applicationId=" + application.getId()
            );
        }
    }

    private ParticipationApplicationStatus mapRecommendationToStatus(String recommendation) {
        return switch (recommendation) {
            case "APPROVE" -> ParticipationApplicationStatus.AUTO_APPROVED;
            case "MANUAL_REVIEW" -> ParticipationApplicationStatus.MANUAL_REVIEW;
            case "REJECT" -> ParticipationApplicationStatus.REJECTED;
            default -> throw new IllegalArgumentException("Unsupported recommendation: " + recommendation);
        };
    }
}