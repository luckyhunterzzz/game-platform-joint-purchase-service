package com.gameplatform.jointpurchaseservice.service;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseOffer;
import com.gameplatform.jointpurchaseservice.domain.entity.ParticipationApplication;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationApplicationStatus;
import com.gameplatform.jointpurchaseservice.kafka.event.ParticipationApplicationSubmittedEvent;
import com.gameplatform.jointpurchaseservice.kafka.producer.JointPurchaseEventProducer;
import com.gameplatform.jointpurchaseservice.repository.JointPurchaseOfferRepository;
import com.gameplatform.jointpurchaseservice.repository.ParticipationApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParticipationApplicationService {

    private final ParticipationApplicationRepository participationApplicationRepository;
    private final JointPurchaseOfferRepository jointPurchaseOfferRepository;
    private final JointPurchaseEventProducer jointPurchaseEventProducer;
    private final Clock clock;

    @Transactional
    public ParticipationApplication submitApplication(UUID offerId, UUID applicantUserId) {
        JointPurchaseOffer offer = jointPurchaseOfferRepository.findById(offerId)
                // TODO: replace with custom exception + RestControllerAdvice + ExceptionHandler
                .orElseThrow(() -> new IllegalArgumentException("Offer not found"));

        if (participationApplicationRepository.existsByOfferIdAndApplicantUserId(offerId, applicantUserId)) {
            throw new IllegalArgumentException("Application already exists for this user and offer");
        }

        OffsetDateTime now = OffsetDateTime.now(clock);

        ParticipationApplication application = ParticipationApplication.builder()
                .id(UUID.randomUUID())
                .offerId(offer.getId())
                .applicantUserId(applicantUserId)
                .status(ParticipationApplicationStatus.SUBMITTED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        ParticipationApplication savedApplication = participationApplicationRepository.save(application);

        ParticipationApplicationSubmittedEvent event = ParticipationApplicationSubmittedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(now)
                .applicationId(savedApplication.getId())
                .offerId(savedApplication.getOfferId())
                .applicantUserId(savedApplication.getApplicantUserId())
                .build();
        // TODO: replace with Outbox Pattern to ensure transactional consistency
        jointPurchaseEventProducer.publishParticipationApplicationSubmitted(event);

        return savedApplication;
    }
}