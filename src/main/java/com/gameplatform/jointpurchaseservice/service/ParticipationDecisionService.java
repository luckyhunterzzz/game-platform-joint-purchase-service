package com.gameplatform.jointpurchaseservice.service;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseOffer;
import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseParticipant;
import com.gameplatform.jointpurchaseservice.domain.entity.ParticipationApplication;
import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseOfferStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseParticipantStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationApplicationStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationType;
import com.gameplatform.jointpurchaseservice.exception.ConflictException;
import com.gameplatform.jointpurchaseservice.repository.jpa.JointPurchaseOfferRepository;
import com.gameplatform.jointpurchaseservice.repository.jpa.JointPurchaseParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParticipationDecisionService {

    private static final List<String> ACTIVE_OFFER_STATUSES = List.of(
            JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS.name(),
            JointPurchaseOfferStatus.MAIN_GROUP_FILLED.name(),
            JointPurchaseOfferStatus.READY_TO_START.name(),
            JointPurchaseOfferStatus.IN_PROGRESS.name()
    );

    private final JointPurchaseParticipantRepository jointPurchaseParticipantRepository;
    private final JointPurchaseOfferRepository jointPurchaseOfferRepository;

    public void approveApplication(
            JointPurchaseOffer offer,
            ParticipationApplication application,
            ParticipationType participationType,
            UUID reviewerUserId,
            OffsetDateTime now
    ) {
        validateAssignable(offer, application, participationType);

        JointPurchaseParticipant participant = JointPurchaseParticipant.builder()
                .id(UUID.randomUUID())
                .offerId(offer.getId())
                .applicationId(application.getId())
                .userId(application.getApplicantUserId())
                .participationType(participationType)
                .status(JointPurchaseParticipantStatus.ACTIVE)
                .joinedAt(now)
                .updatedAt(now)
                .build();

        jointPurchaseParticipantRepository.save(participant);

        application.setAssignedParticipationType(participationType);
        application.setReviewedByUserId(reviewerUserId);
        application.setReviewedAt(now);
        application.setUpdatedAt(now);

        if (participationType == ParticipationType.MAIN) {
            offer.setCurrentMainParticipants(offer.getCurrentMainParticipants() + 1);
            application.setStatus(ParticipationApplicationStatus.APPROVED_MAIN);

            if (offer.getCurrentMainParticipants() >= offer.getRequiredParticipants()) {
                offer.setStatus(JointPurchaseOfferStatus.MAIN_GROUP_FILLED);
            }
        } else {
            offer.setCurrentReserveParticipants(offer.getCurrentReserveParticipants() + 1);
            application.setStatus(ParticipationApplicationStatus.APPROVED_RESERVE);
        }

        offer.setUpdatedAt(now);
    }

    public void rejectApplication(
            ParticipationApplication application,
            UUID reviewerUserId,
            OffsetDateTime now
    ) {
        application.setStatus(ParticipationApplicationStatus.REJECTED);
        application.setReviewedByUserId(reviewerUserId);
        application.setReviewedAt(now);
        application.setUpdatedAt(now);
    }

    public void moveParticipant(
            JointPurchaseOffer offer,
            ParticipationApplication application,
            JointPurchaseParticipant participant,
            ParticipationType targetType,
            UUID reviewerUserId,
            OffsetDateTime now
    ) {
        if (participant.getParticipationType() == targetType) {
            return;
        }

        if (targetType == ParticipationType.MAIN) {
            validateMoveToMain(offer, application);
            offer.setCurrentReserveParticipants(Math.max(0, offer.getCurrentReserveParticipants() - 1));
            offer.setCurrentMainParticipants(offer.getCurrentMainParticipants() + 1);
            participant.setParticipationType(ParticipationType.MAIN);
            application.setAssignedParticipationType(ParticipationType.MAIN);
            application.setStatus(ParticipationApplicationStatus.APPROVED_MAIN);

            if (offer.getCurrentMainParticipants() >= offer.getRequiredParticipants()) {
                offer.setStatus(JointPurchaseOfferStatus.MAIN_GROUP_FILLED);
            }
        } else {
            validateMoveToReserve(offer);
            offer.setCurrentMainParticipants(Math.max(0, offer.getCurrentMainParticipants() - 1));
            offer.setCurrentReserveParticipants(offer.getCurrentReserveParticipants() + 1);
            participant.setParticipationType(ParticipationType.RESERVE);
            application.setAssignedParticipationType(ParticipationType.RESERVE);
            application.setStatus(ParticipationApplicationStatus.APPROVED_RESERVE);

            if (offer.getStatus() == JointPurchaseOfferStatus.MAIN_GROUP_FILLED
                    && offer.getCurrentMainParticipants() < offer.getRequiredParticipants()) {
                offer.setStatus(JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS);
            }
        }

        participant.setUpdatedAt(now);
        application.setReviewedByUserId(reviewerUserId);
        application.setReviewedAt(now);
        application.setUpdatedAt(now);
        offer.setUpdatedAt(now);
    }

    private void validateAssignable(
            JointPurchaseOffer offer,
            ParticipationApplication application,
            ParticipationType participationType
    ) {
        if (jointPurchaseParticipantRepository.existsByOfferIdAndUserIdAndStatus(
                offer.getId(),
                application.getApplicantUserId(),
                JointPurchaseParticipantStatus.ACTIVE
        )) {
            throw new ConflictException("User already participates in this offer");
        }

        if (participationType == ParticipationType.MAIN) {
            if (offer.getCurrentMainParticipants() >= offer.getRequiredParticipants()) {
                throw new ConflictException("MAIN group is already full");
            }

            if (jointPurchaseParticipantRepository.existsByUserIdInActiveMainOffers(
                    application.getApplicantUserId(),
                    ACTIVE_OFFER_STATUSES
            )) {
                releaseInactiveMainParticipations(application.getApplicantUserId(), OffsetDateTime.now());

                if (jointPurchaseParticipantRepository.existsByUserIdInActiveMainOffers(
                        application.getApplicantUserId(),
                        ACTIVE_OFFER_STATUSES
                )) {
                    throw new ConflictException("User already has active MAIN participation");
                }
            }
        } else if (offer.getCurrentReserveParticipants() >= offer.getReserveParticipants()) {
            throw new ConflictException("RESERVE group is already full");
        }
    }

    private void validateMoveToMain(JointPurchaseOffer offer, ParticipationApplication application) {
        if (offer.getCurrentMainParticipants() >= offer.getRequiredParticipants()) {
            throw new ConflictException("MAIN group is already full");
        }

        if (jointPurchaseParticipantRepository.existsByUserIdInActiveMainOffers(
                application.getApplicantUserId(),
                ACTIVE_OFFER_STATUSES
        )) {
            releaseInactiveMainParticipations(application.getApplicantUserId(), OffsetDateTime.now());

            if (!jointPurchaseParticipantRepository.existsByUserIdInActiveMainOffers(
                    application.getApplicantUserId(),
                    ACTIVE_OFFER_STATUSES
            )) {
                return;
            }

            throw new ConflictException("User already has active MAIN participation");
        }
    }

    private void validateMoveToReserve(JointPurchaseOffer offer) {
        if (offer.getCurrentReserveParticipants() >= offer.getReserveParticipants()) {
            throw new ConflictException("RESERVE group is already full");
        }
    }

    private void releaseInactiveMainParticipations(UUID userId, OffsetDateTime now) {
        List<JointPurchaseParticipant> mainParticipations = jointPurchaseParticipantRepository
                .findAllByUserIdAndParticipationTypeAndStatus(
                        userId,
                        ParticipationType.MAIN,
                        JointPurchaseParticipantStatus.ACTIVE
                );

        if (mainParticipations.isEmpty()) {
            return;
        }

        List<UUID> offerIds = mainParticipations.stream()
                .map(JointPurchaseParticipant::getOfferId)
                .toList();

        List<JointPurchaseOffer> offers = jointPurchaseOfferRepository.findAllById(offerIds);

        List<UUID> inactiveOfferIds = offers.stream()
                .filter(offer -> !ACTIVE_OFFER_STATUSES.contains(offer.getStatus().name()))
                .map(JointPurchaseOffer::getId)
                .toList();

        if (inactiveOfferIds.isEmpty()) {
            return;
        }

        List<JointPurchaseParticipant> staleParticipations = mainParticipations.stream()
                .filter(participant -> inactiveOfferIds.contains(participant.getOfferId()))
                .toList();

        if (staleParticipations.isEmpty()) {
            return;
        }

        staleParticipations.forEach(participant -> {
                    participant.setStatus(resolveReleasedStatus(participant.getOfferId(), offers));
                    participant.setUpdatedAt(now);
                });

        jointPurchaseParticipantRepository.saveAll(staleParticipations);
    }

    private JointPurchaseParticipantStatus resolveReleasedStatus(
            UUID offerId,
            List<JointPurchaseOffer> offers
    ) {
        return offers.stream()
                .filter(offer -> offer.getId().equals(offerId))
                .findFirst()
                .map(offer -> offer.getStatus() == JointPurchaseOfferStatus.CANCELLED
                        ? JointPurchaseParticipantStatus.REMOVED
                        : JointPurchaseParticipantStatus.COMPLETED)
                .orElse(JointPurchaseParticipantStatus.REMOVED);
    }
}
