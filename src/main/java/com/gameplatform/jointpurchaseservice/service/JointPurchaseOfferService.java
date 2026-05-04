package com.gameplatform.jointpurchaseservice.service;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseOffer;
import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseParticipant;
import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseOfferStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseParticipantStatus;
import com.gameplatform.jointpurchaseservice.dto.request.CreateJointPurchaseOfferRequestDto;
import com.gameplatform.jointpurchaseservice.exception.BadRequestException;
import com.gameplatform.jointpurchaseservice.exception.ConflictException;
import com.gameplatform.jointpurchaseservice.exception.ForbiddenException;
import com.gameplatform.jointpurchaseservice.exception.NotFoundException;
import com.gameplatform.jointpurchaseservice.integration.playerprofile.PlayerProfileClient;
import com.gameplatform.jointpurchaseservice.repository.jpa.JointPurchaseParticipantRepository;
import com.gameplatform.jointpurchaseservice.repository.jpa.JointPurchaseOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JointPurchaseOfferService {

    private static final List<String> ACTIVE_OFFER_STATUSES = List.of(
            JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS.name(),
            JointPurchaseOfferStatus.MAIN_GROUP_FILLED.name(),
            JointPurchaseOfferStatus.READY_TO_START.name(),
            JointPurchaseOfferStatus.IN_PROGRESS.name()
    );

    private final JointPurchaseOfferRepository jointPurchaseOfferRepository;
    private final JointPurchaseParticipantRepository jointPurchaseParticipantRepository;
    private final ParticipantFeedbackService participantFeedbackService;
    private final PlayerProfileClient playerProfileClient;
    private final Clock clock;

    @Transactional
    public JointPurchaseOffer createOffer(
            UUID organizerUserId,
            String organizerEmail,
            List<String> organizerRoles,
            CreateJointPurchaseOfferRequestDto requestDto
    ) {
        validatePlannedWindow(requestDto);
        validateOrganizerCanCreateOffer(organizerUserId, organizerEmail, organizerRoles);

        OffsetDateTime now = OffsetDateTime.now(clock);

        JointPurchaseOffer jointPurchaseOffer = JointPurchaseOffer.builder()
                .id(UUID.randomUUID())
                .organizerUserId(organizerUserId)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .allianceName(requestDto.getAllianceName())
                .screenshotBucket(requestDto.getScreenshotBucket())
                .screenshotObjectKey(requestDto.getScreenshotObjectKey())
                .requiredParticipants(requestDto.getRequiredParticipants())
                .reserveParticipants(requestDto.getReserveParticipants())
                .currentMainParticipants(0)
                .currentReserveParticipants(0)
                .autoApproveEnabled(requestDto.getAutoApproveEnabled())
                .status(JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS)
                .plannedStartAt(requestDto.getPlannedStartAt())
                .plannedEndAt(requestDto.getPlannedEndAt())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return jointPurchaseOfferRepository.save(jointPurchaseOffer);
    }

    @Transactional(readOnly = true)
    public List<JointPurchaseOffer> getOrganizerOffers(UUID organizerUserId) {
        return jointPurchaseOfferRepository.findAllByOrganizerUserIdOrderByCreatedAtDesc(organizerUserId);
    }

    @Transactional(readOnly = true)
    public List<JointPurchaseOffer> getOpenOffers() {
        return jointPurchaseOfferRepository.findAllByStatusOrderByCreatedAtDesc(
                JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS
        );
    }

    @Transactional
    public JointPurchaseOffer updateOfferStatus(
            UUID organizerUserId,
            UUID offerId,
            JointPurchaseOfferStatus targetStatus
    ) {
        JointPurchaseOffer offer = getOfferOrThrow(offerId);

        if (!offer.getOrganizerUserId().equals(organizerUserId)) {
            throw new ForbiddenException("Only the creator can change offer status");
        }

        validateStatusTransition(offer, targetStatus);

        if (targetStatus == JointPurchaseOfferStatus.COMPLETED) {
            participantFeedbackService.assertOfferCanBeCompleted(offer);
        }

        completeOrReleaseParticipantsIfNeeded(offer, targetStatus);
        offer.setStatus(targetStatus);
        offer.setUpdatedAt(OffsetDateTime.now(clock));

        return jointPurchaseOfferRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public JointPurchaseOffer getOfferOrThrow(UUID offerId) {
        return jointPurchaseOfferRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer not found: " + offerId));
    }

    private void validatePlannedWindow(CreateJointPurchaseOfferRequestDto requestDto) {
        if (requestDto.getPlannedEndAt().isBefore(requestDto.getPlannedStartAt())
                || requestDto.getPlannedEndAt().isEqual(requestDto.getPlannedStartAt())) {
            throw new BadRequestException("plannedEndAt must be after plannedStartAt");
        }
    }

    private void validateOrganizerCanCreateOffer(
            UUID organizerUserId,
            String organizerEmail,
            List<String> organizerRoles
    ) {
        List<JointPurchaseOfferStatus> activeOfferStatuses = Arrays.asList(
                JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS,
                JointPurchaseOfferStatus.MAIN_GROUP_FILLED,
                JointPurchaseOfferStatus.READY_TO_START,
                JointPurchaseOfferStatus.IN_PROGRESS
        );

        boolean isAdmin = organizerRoles.contains("ROLE_admin");
        boolean isSuperAdmin = organizerRoles.contains("ROLE_superadmin");
        boolean isContractor = organizerRoles.contains("ROLE_contractor");

        if (isContractor && !isAdmin && !isSuperAdmin
                && !playerProfileClient.isProfileComplete(organizerUserId, organizerEmail)) {
            throw new ForbiddenException("Contractor profile must be COMPLETE to create a joint purchase offer");
        }

        if (jointPurchaseOfferRepository.existsByOrganizerUserIdAndStatusIn(organizerUserId, activeOfferStatuses)) {
            throw new ConflictException("Organizer can have only one active joint purchase offer at a time");
        }

        if (jointPurchaseParticipantRepository.existsByUserIdInActiveOffers(
                organizerUserId,
                ACTIVE_OFFER_STATUSES
        )) {
            throw new ConflictException("User cannot create an offer while participating in another active purchase");
        }
    }

    private void validateStatusTransition(
            JointPurchaseOffer offer,
            JointPurchaseOfferStatus targetStatus
    ) {
        JointPurchaseOfferStatus currentStatus = offer.getStatus();

        if (currentStatus == targetStatus) {
            return;
        }

        boolean allowed = switch (currentStatus) {
            case OPEN_FOR_APPLICATIONS ->
                    targetStatus == JointPurchaseOfferStatus.CANCELLED
                            || targetStatus == JointPurchaseOfferStatus.READY_TO_START
                            || (targetStatus == JointPurchaseOfferStatus.MAIN_GROUP_FILLED
                            && offer.getCurrentMainParticipants() >= offer.getRequiredParticipants());
            case MAIN_GROUP_FILLED ->
                    targetStatus == JointPurchaseOfferStatus.READY_TO_START
                            || targetStatus == JointPurchaseOfferStatus.CANCELLED;
            case READY_TO_START ->
                    targetStatus == JointPurchaseOfferStatus.IN_PROGRESS
                            || targetStatus == JointPurchaseOfferStatus.CANCELLED;
            case IN_PROGRESS ->
                    targetStatus == JointPurchaseOfferStatus.COMPLETED
                            || targetStatus == JointPurchaseOfferStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new BadRequestException(
                    "Invalid offer status transition: " + currentStatus + " -> " + targetStatus
            );
        }
    }

    private void completeOrReleaseParticipantsIfNeeded(
            JointPurchaseOffer offer,
            JointPurchaseOfferStatus targetStatus
    ) {
        if (targetStatus != JointPurchaseOfferStatus.COMPLETED
                && targetStatus != JointPurchaseOfferStatus.CANCELLED) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        JointPurchaseParticipantStatus targetParticipantStatus =
                targetStatus == JointPurchaseOfferStatus.COMPLETED
                        ? JointPurchaseParticipantStatus.COMPLETED
                        : JointPurchaseParticipantStatus.REMOVED;

        List<JointPurchaseParticipant> activeParticipants =
                jointPurchaseParticipantRepository.findAllByOfferIdAndStatusOrderByJoinedAtAsc(
                        offer.getId(),
                        JointPurchaseParticipantStatus.ACTIVE
                );

        activeParticipants.forEach(participant -> {
            participant.setStatus(targetParticipantStatus);
            participant.setUpdatedAt(now);
        });

        if (!activeParticipants.isEmpty()) {
            jointPurchaseParticipantRepository.saveAll(activeParticipants);
        }
    }
}
