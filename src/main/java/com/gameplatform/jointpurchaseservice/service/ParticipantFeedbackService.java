package com.gameplatform.jointpurchaseservice.service;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseOffer;
import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseParticipant;
import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseParticipantFeedback;
import com.gameplatform.jointpurchaseservice.domain.entity.ParticipationApplication;
import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseOfferStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseParticipantStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationType;
import com.gameplatform.jointpurchaseservice.dto.request.UpsertParticipantFeedbackRequestDto;
import com.gameplatform.jointpurchaseservice.exception.BadRequestException;
import com.gameplatform.jointpurchaseservice.exception.ConflictException;
import com.gameplatform.jointpurchaseservice.exception.ForbiddenException;
import com.gameplatform.jointpurchaseservice.exception.NotFoundException;
import com.gameplatform.jointpurchaseservice.repository.jpa.JointPurchaseParticipantFeedbackRepository;
import com.gameplatform.jointpurchaseservice.repository.jpa.JointPurchaseParticipantRepository;
import com.gameplatform.jointpurchaseservice.repository.jpa.JointPurchaseOfferRepository;
import com.gameplatform.jointpurchaseservice.repository.jpa.ParticipationApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParticipantFeedbackService {

    private final JointPurchaseOfferRepository jointPurchaseOfferRepository;
    private final ParticipationApplicationRepository participationApplicationRepository;
    private final JointPurchaseParticipantRepository jointPurchaseParticipantRepository;
    private final JointPurchaseParticipantFeedbackRepository jointPurchaseParticipantFeedbackRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<JointPurchaseParticipantFeedback> getOfferFeedback(UUID organizerUserId, UUID offerId) {
        getOfferAndAssertOwnership(organizerUserId, offerId);
        return jointPurchaseParticipantFeedbackRepository.findAllByOfferIdOrderByCreatedAtAsc(offerId);
    }

    @Transactional
    public JointPurchaseParticipantFeedback upsertFeedback(
            UUID organizerUserId,
            UUID offerId,
            UUID applicationId,
            UpsertParticipantFeedbackRequestDto requestDto
    ) {
        JointPurchaseOffer offer = getOfferAndAssertOwnership(organizerUserId, offerId);

        if (offer.getStatus() == JointPurchaseOfferStatus.COMPLETED) {
            throw new ConflictException("Feedback is locked after the purchase is completed");
        }

        ParticipationApplication application = participationApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found: " + applicationId));

        if (!application.getOfferId().equals(offerId)) {
            throw new BadRequestException("Application does not belong to the specified offer");
        }

        JointPurchaseParticipant participant = jointPurchaseParticipantRepository
                .findByApplicationIdAndStatus(applicationId, JointPurchaseParticipantStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Active participant not found for application: " + applicationId));

        if (participant.getParticipationType() != ParticipationType.MAIN) {
            throw new ConflictException("Feedback can be left only for MAIN participants");
        }

        OffsetDateTime now = OffsetDateTime.now(clock);

        JointPurchaseParticipantFeedback feedback = jointPurchaseParticipantFeedbackRepository
                .findByOfferIdAndApplicationId(offerId, applicationId)
                .map(existing -> updateFeedback(existing, organizerUserId, requestDto, now))
                .orElseGet(() -> createFeedback(offerId, application, organizerUserId, requestDto, now));

        return jointPurchaseParticipantFeedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public void assertOfferCanBeCompleted(JointPurchaseOffer offer) {
        List<UUID> mainApplicationIds = jointPurchaseParticipantRepository
                .findAllByOfferIdAndParticipationTypeAndStatus(
                        offer.getId(),
                        ParticipationType.MAIN,
                        JointPurchaseParticipantStatus.ACTIVE
                )
                .stream()
                .map(JointPurchaseParticipant::getApplicationId)
                .toList();

        if (mainApplicationIds.isEmpty()) {
            throw new ConflictException("Offer cannot be completed without active MAIN participants");
        }

        long feedbackCount = jointPurchaseParticipantFeedbackRepository.countByOfferIdAndApplicationIdIn(
                offer.getId(),
                mainApplicationIds
        );

        if (feedbackCount != mainApplicationIds.size()) {
            throw new ConflictException("Feedback must be provided for all MAIN participants before completion");
        }
    }

    private JointPurchaseOffer getOfferAndAssertOwnership(UUID organizerUserId, UUID offerId) {
        JointPurchaseOffer offer = jointPurchaseOfferRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer not found: " + offerId));

        if (!offer.getOrganizerUserId().equals(organizerUserId)) {
            throw new ForbiddenException("Only the creator can manage participant feedback");
        }

        return offer;
    }

    private JointPurchaseParticipantFeedback createFeedback(
            UUID offerId,
            ParticipationApplication application,
            UUID organizerUserId,
            UpsertParticipantFeedbackRequestDto requestDto,
            OffsetDateTime now
    ) {
        return JointPurchaseParticipantFeedback.builder()
                .id(UUID.randomUUID())
                .offerId(offerId)
                .applicationId(application.getId())
                .participantUserId(application.getApplicantUserId())
                .authorUserId(organizerUserId)
                .result(requestDto.getResult())
                .description(normalizeDescription(requestDto.getDescription()))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private JointPurchaseParticipantFeedback updateFeedback(
            JointPurchaseParticipantFeedback feedback,
            UUID organizerUserId,
            UpsertParticipantFeedbackRequestDto requestDto,
            OffsetDateTime now
    ) {
        feedback.setAuthorUserId(organizerUserId);
        feedback.setResult(requestDto.getResult());
        feedback.setDescription(normalizeDescription(requestDto.getDescription()));
        feedback.setUpdatedAt(now);
        return feedback;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}
