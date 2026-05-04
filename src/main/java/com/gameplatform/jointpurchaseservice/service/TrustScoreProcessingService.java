package com.gameplatform.jointpurchaseservice.service;

import com.gameplatform.jointpurchaseservice.domain.document.TrustScoreSnapshotDocument;
import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseOffer;
import com.gameplatform.jointpurchaseservice.domain.entity.ParticipationApplication;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationApplicationStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationType;
import com.gameplatform.jointpurchaseservice.exception.ConflictException;
import com.gameplatform.jointpurchaseservice.kafka.event.TrustScoreCalculatedEvent;
import com.gameplatform.jointpurchaseservice.repository.jpa.JointPurchaseOfferRepository;
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
    private final JointPurchaseOfferRepository jointPurchaseOfferRepository;
    private final TrustScoreSnapshotMongoRepository trustScoreSnapshotMongoRepository;
    private final ParticipationDecisionService participationDecisionService;
    private final Clock clock;

    @Transactional
    public void processTrustScoreCalculatedEvent(TrustScoreCalculatedEvent event) {
        ParticipationApplication application = participationApplicationRepository.findById(event.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Participation application not found: " + event.getApplicationId()
                ));
        JointPurchaseOffer offer = jointPurchaseOfferRepository.findById(event.getOfferId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Joint purchase offer not found: " + event.getOfferId()
                ));

        validateEventMatchesApplication(application, event);

        OffsetDateTime now = OffsetDateTime.now(clock);

        applyRecommendation(offer, application, event.getRecommendation(), now);

        jointPurchaseOfferRepository.save(offer);
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
                application.getStatus(),
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

    private void applyRecommendation(
            JointPurchaseOffer offer,
            ParticipationApplication application,
            String recommendation,
            OffsetDateTime now
    ) {
        switch (recommendation) {
            case "APPROVE" -> approveAutomatically(offer, application, now);
            case "MANUAL_REVIEW" -> {
                application.setStatus(ParticipationApplicationStatus.PENDING_ORGANIZER_REVIEW);
                application.setUpdatedAt(now);
            }
            case "REJECT" -> {
                application.setStatus(ParticipationApplicationStatus.REJECTED);
                application.setUpdatedAt(now);
            }
            default -> throw new IllegalArgumentException("Unsupported recommendation: " + recommendation);
        }
    }

    private void approveAutomatically(
            JointPurchaseOffer offer,
            ParticipationApplication application,
            OffsetDateTime now
    ) {
        if (offer.getCurrentMainParticipants() < offer.getRequiredParticipants()) {
            try {
                participationDecisionService.approveApplication(offer, application, ParticipationType.MAIN, null, now);
                return;
            } catch (ConflictException ignored) {
                // Fallback to reserve or organizer review when applicant cannot be assigned to main automatically.
            }
        }

        if (offer.getCurrentReserveParticipants() < offer.getReserveParticipants()) {
            try {
                participationDecisionService.approveApplication(offer, application, ParticipationType.RESERVE, null, now);
                return;
            } catch (ConflictException ignored) {
                // Fallback to organizer review when reserve assignment is also not possible.
            }
        }

        application.setStatus(ParticipationApplicationStatus.PENDING_ORGANIZER_REVIEW);
        application.setUpdatedAt(now);
    }
}
