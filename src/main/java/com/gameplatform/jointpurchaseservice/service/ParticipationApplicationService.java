package com.gameplatform.jointpurchaseservice.service;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseOffer;
import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseParticipant;
import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseOfferStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseParticipantStatus;
import com.gameplatform.jointpurchaseservice.domain.entity.ParticipationApplication;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationApplicationStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationType;
import com.gameplatform.jointpurchaseservice.dto.response.ParticipationApplicationDetailsResponseDto;
import com.gameplatform.jointpurchaseservice.dto.response.PlayerProfileDetailsResponseDto;
import com.gameplatform.jointpurchaseservice.dto.request.SubmitParticipationApplicationRequestDto;
import com.gameplatform.jointpurchaseservice.exception.BadRequestException;
import com.gameplatform.jointpurchaseservice.exception.ConflictException;
import com.gameplatform.jointpurchaseservice.exception.ForbiddenException;
import com.gameplatform.jointpurchaseservice.exception.NotFoundException;
import com.gameplatform.jointpurchaseservice.integration.playerprofile.PlayerProfileClient;
import com.gameplatform.jointpurchaseservice.integration.playerprofile.PlayerProfileResponse;
import com.gameplatform.jointpurchaseservice.kafka.event.ParticipationApplicationSubmittedEvent;
import com.gameplatform.jointpurchaseservice.kafka.producer.JointPurchaseEventProducer;
import com.gameplatform.jointpurchaseservice.mapper.PlayerProfileDetailsMapper;
import com.gameplatform.jointpurchaseservice.media.service.MediaStorageService;
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
public class ParticipationApplicationService {

    private final ParticipationApplicationRepository participationApplicationRepository;
    private final JointPurchaseOfferRepository jointPurchaseOfferRepository;
    private final JointPurchaseParticipantRepository jointPurchaseParticipantRepository;
    private final JointPurchaseEventProducer jointPurchaseEventProducer;
    private final ParticipationDecisionService participationDecisionService;
    private final PlayerProfileClient playerProfileClient;
    private final PlayerProfileDetailsMapper playerProfileDetailsMapper;
    private final MediaStorageService mediaStorageService;
    private final Clock clock;

    @Transactional
    public ParticipationApplication submitApplication(
            UUID offerId,
            UUID applicantUserId,
            String applicantEmail,
            SubmitParticipationApplicationRequestDto requestDto
    ) {
        JointPurchaseOffer offer = jointPurchaseOfferRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer not found: " + offerId));

        if (offer.getStatus() != JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS
                && offer.getStatus() != JointPurchaseOfferStatus.MAIN_GROUP_FILLED) {
            throw new ConflictException("Applications are not accepted for this offer");
        }

        if (offer.getOrganizerUserId().equals(applicantUserId)) {
            throw new ForbiddenException("Organizer cannot participate in their own joint purchase");
        }

        if (participationApplicationRepository.existsByOfferIdAndApplicantUserId(offerId, applicantUserId)) {
            throw new ConflictException("Application already exists for this user and offer");
        }

        if (jointPurchaseOfferRepository.existsByOrganizerUserIdAndStatusIn(
                applicantUserId,
                List.of(
                        JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS,
                        JointPurchaseOfferStatus.MAIN_GROUP_FILLED,
                        JointPurchaseOfferStatus.READY_TO_START,
                        JointPurchaseOfferStatus.IN_PROGRESS
                )
        )) {
            throw new ConflictException("Organizer cannot participate while having an active joint purchase offer");
        }

        if (!playerProfileClient.isProfileComplete(applicantUserId, applicantEmail)) {
            throw new ForbiddenException("Profile must be COMPLETE to participate in joint purchases");
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        ParticipationApplicationStatus initialStatus = offer.getAutoApproveEnabled()
                ? ParticipationApplicationStatus.PENDING_TRUST_CHECK
                : ParticipationApplicationStatus.PENDING_ORGANIZER_REVIEW;

        ParticipationApplication application = ParticipationApplication.builder()
                .id(UUID.randomUUID())
                .offerId(offer.getId())
                .applicantUserId(applicantUserId)
                .status(initialStatus)
                .screenshotBucket(requestDto.getScreenshotBucket())
                .screenshotObjectKey(requestDto.getScreenshotObjectKey())
                .createdAt(now)
                .updatedAt(now)
                .build();

        ParticipationApplication savedApplication = participationApplicationRepository.save(application);

        if (offer.getAutoApproveEnabled()) {
            ParticipationApplicationSubmittedEvent event = ParticipationApplicationSubmittedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .occurredAt(now)
                    .applicationId(savedApplication.getId())
                    .offerId(savedApplication.getOfferId())
                    .applicantUserId(savedApplication.getApplicantUserId())
                    .build();
            jointPurchaseEventProducer.publishParticipationApplicationSubmitted(event);
        }

        return savedApplication;
    }

    @Transactional(readOnly = true)
    public List<ParticipationApplication> getOfferApplications(UUID organizerUserId, UUID offerId) {
        JointPurchaseOffer offer = jointPurchaseOfferRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer not found: " + offerId));

        if (!offer.getOrganizerUserId().equals(organizerUserId)) {
            throw new ForbiddenException("Only the creator can access offer applications");
        }

        return participationApplicationRepository.findAllByOfferIdOrderByCreatedAtAsc(offerId);
    }

    @Transactional(readOnly = true)
    public ParticipationApplication getCurrentUserApplication(UUID offerId, UUID applicantUserId) {
        return participationApplicationRepository.findByOfferIdAndApplicantUserId(offerId, applicantUserId)
                .orElse(null);
    }

    @Transactional
    public ParticipationApplication cancelOwnApplication(UUID applicantUserId, UUID offerId) {
        JointPurchaseOffer offer = jointPurchaseOfferRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer not found: " + offerId));

        ParticipationApplication application = participationApplicationRepository
                .findByOfferIdAndApplicantUserId(offerId, applicantUserId)
                .orElseThrow(() -> new NotFoundException("Application not found for user and offer"));

        if (offer.getStatus() != JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS
                && offer.getStatus() != JointPurchaseOfferStatus.MAIN_GROUP_FILLED) {
            throw new ConflictException("Application can be cancelled only before the purchase is ready");
        }

        if (application.getStatus() == ParticipationApplicationStatus.REJECTED
                || application.getStatus() == ParticipationApplicationStatus.CANCELLED) {
            throw new ConflictException("Application has already been processed");
        }

        OffsetDateTime now = OffsetDateTime.now(clock);

        if (application.getStatus() == ParticipationApplicationStatus.APPROVED_MAIN
                || application.getStatus() == ParticipationApplicationStatus.APPROVED_RESERVE) {
            JointPurchaseParticipant participant = jointPurchaseParticipantRepository
                    .findByApplicationIdAndStatus(application.getId(), JointPurchaseParticipantStatus.ACTIVE)
                    .orElseThrow(() -> new NotFoundException("Active participant not found for application: " + application.getId()));

            if (participant.getParticipationType() == ParticipationType.MAIN) {
                offer.setCurrentMainParticipants(Math.max(0, offer.getCurrentMainParticipants() - 1));
                if (offer.getStatus() == JointPurchaseOfferStatus.MAIN_GROUP_FILLED
                        && offer.getCurrentMainParticipants() < offer.getRequiredParticipants()) {
                    offer.setStatus(JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS);
                }
            } else {
                offer.setCurrentReserveParticipants(Math.max(0, offer.getCurrentReserveParticipants() - 1));
            }

            participant.setStatus(JointPurchaseParticipantStatus.REMOVED);
            participant.setUpdatedAt(now);
            jointPurchaseParticipantRepository.save(participant);
        }

        application.setStatus(ParticipationApplicationStatus.CANCELLED);
        application.setUpdatedAt(now);
        offer.setUpdatedAt(now);

        jointPurchaseOfferRepository.save(offer);
        return participationApplicationRepository.save(application);
    }

    @Transactional
    public ParticipationApplication approveApplication(
            UUID organizerUserId,
            UUID offerId,
            UUID applicationId,
            ParticipationType participationType
    ) {
        JointPurchaseOffer offer = getOfferAndAssertOwnership(organizerUserId, offerId);
        ParticipationApplication application = getApplicationAndAssertOffer(offerId, applicationId);
        validateReviewableApplication(offerId, application);

        OffsetDateTime now = OffsetDateTime.now(clock);
        participationDecisionService.approveApplication(offer, application, participationType, organizerUserId, now);

        jointPurchaseOfferRepository.save(offer);
        return participationApplicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public ParticipationApplicationDetailsResponseDto getApplicationDetails(
            UUID organizerUserId,
            UUID offerId,
            UUID applicationId
    ) {
        getOfferAndAssertOwnership(organizerUserId, offerId);
        ParticipationApplication application = getApplicationAndAssertOffer(offerId, applicationId);
        PlayerProfileResponse playerProfile = playerProfileClient.getProfileByUserId(application.getApplicantUserId());
        PlayerProfileDetailsResponseDto profileResponseDto = playerProfileDetailsMapper.toResponseDto(playerProfile);

        return ParticipationApplicationDetailsResponseDto.builder()
                .id(application.getId())
                .offerId(application.getOfferId())
                .applicantUserId(application.getApplicantUserId())
                .status(application.getStatus())
                .assignedParticipationType(application.getAssignedParticipationType())
                .reviewedByUserId(application.getReviewedByUserId())
                .reviewedAt(application.getReviewedAt())
                .screenshotBucket(application.getScreenshotBucket())
                .screenshotObjectKey(application.getScreenshotObjectKey())
                .screenshotUrl(mediaStorageService.createApplicationScreenshotAccessUrl(
                        application.getScreenshotBucket(),
                        application.getScreenshotObjectKey()
                ))
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .playerProfile(profileResponseDto)
                .build();
    }

    @Transactional(readOnly = true)
    public String getApplicationScreenshotAccessUrl(
            UUID organizerUserId,
            UUID offerId,
            UUID applicationId
    ) {
        getOfferAndAssertOwnership(organizerUserId, offerId);
        ParticipationApplication application = getApplicationAndAssertOffer(offerId, applicationId);

        return mediaStorageService.createApplicationScreenshotAccessUrl(
                application.getScreenshotBucket(),
                application.getScreenshotObjectKey()
        );
    }

    @Transactional
    public ParticipationApplication moveApprovedApplication(
            UUID organizerUserId,
            UUID offerId,
            UUID applicationId,
            ParticipationType targetType
    ) {
        JointPurchaseOffer offer = getOfferAndAssertOwnership(organizerUserId, offerId);
        ParticipationApplication application = getApplicationAndAssertOffer(offerId, applicationId);

        if (offer.getStatus() != JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS
                && offer.getStatus() != JointPurchaseOfferStatus.MAIN_GROUP_FILLED) {
            throw new ConflictException("Participant assignments can be changed only before the purchase is ready");
        }

        if (application.getStatus() != ParticipationApplicationStatus.APPROVED_MAIN
                && application.getStatus() != ParticipationApplicationStatus.APPROVED_RESERVE) {
            throw new ConflictException("Only approved applications can be moved between main and reserve");
        }

        JointPurchaseParticipant participant = jointPurchaseParticipantRepository
                .findByApplicationIdAndStatus(applicationId, JointPurchaseParticipantStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Active participant not found for application: " + applicationId));

        participationDecisionService.moveParticipant(
                offer,
                application,
                participant,
                targetType,
                organizerUserId,
                OffsetDateTime.now(clock)
        );

        jointPurchaseParticipantRepository.save(participant);
        jointPurchaseOfferRepository.save(offer);
        return participationApplicationRepository.save(application);
    }

    @Transactional
    public ParticipationApplication cancelApprovedApplication(
            UUID organizerUserId,
            UUID offerId,
            UUID applicationId
    ) {
        JointPurchaseOffer offer = getOfferAndAssertOwnership(organizerUserId, offerId);
        ParticipationApplication application = getApplicationAndAssertOffer(offerId, applicationId);

        if (offer.getStatus() != JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS
                && offer.getStatus() != JointPurchaseOfferStatus.MAIN_GROUP_FILLED) {
            throw new ConflictException("Approved applications can be cancelled only before the purchase is ready");
        }

        if (application.getStatus() != ParticipationApplicationStatus.APPROVED_MAIN
                && application.getStatus() != ParticipationApplicationStatus.APPROVED_RESERVE) {
            throw new ConflictException("Only approved applications can be cancelled by organizer");
        }

        JointPurchaseParticipant participant = jointPurchaseParticipantRepository
                .findByApplicationIdAndStatus(applicationId, JointPurchaseParticipantStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Active participant not found for application: " + applicationId));

        OffsetDateTime now = OffsetDateTime.now(clock);

        if (participant.getParticipationType() == ParticipationType.MAIN) {
            offer.setCurrentMainParticipants(Math.max(0, offer.getCurrentMainParticipants() - 1));
            if (offer.getStatus() == JointPurchaseOfferStatus.MAIN_GROUP_FILLED
                    && offer.getCurrentMainParticipants() < offer.getRequiredParticipants()) {
                offer.setStatus(JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS);
            }
        } else {
            offer.setCurrentReserveParticipants(Math.max(0, offer.getCurrentReserveParticipants() - 1));
        }

        participant.setStatus(JointPurchaseParticipantStatus.REMOVED);
        participant.setUpdatedAt(now);

        application.setStatus(ParticipationApplicationStatus.CANCELLED);
        application.setAssignedParticipationType(null);
        application.setReviewedByUserId(organizerUserId);
        application.setReviewedAt(now);
        application.setUpdatedAt(now);

        offer.setUpdatedAt(now);

        jointPurchaseParticipantRepository.save(participant);
        jointPurchaseOfferRepository.save(offer);
        return participationApplicationRepository.save(application);
    }

    @Transactional
    public ParticipationApplication rejectApplication(
            UUID organizerUserId,
            UUID offerId,
            UUID applicationId
    ) {
        getOfferAndAssertOwnership(organizerUserId, offerId);
        ParticipationApplication application = getApplicationAndAssertOffer(offerId, applicationId);
        validateReviewableApplication(offerId, application);

        participationDecisionService.rejectApplication(application, organizerUserId, OffsetDateTime.now(clock));
        return participationApplicationRepository.save(application);
    }

    private JointPurchaseOffer getOfferAndAssertOwnership(UUID organizerUserId, UUID offerId) {
        JointPurchaseOffer offer = jointPurchaseOfferRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer not found: " + offerId));

        if (!offer.getOrganizerUserId().equals(organizerUserId)) {
            throw new ForbiddenException("Only the creator can review applications for this offer");
        }

        return offer;
    }

    private ParticipationApplication getApplicationAndAssertOffer(UUID offerId, UUID applicationId) {
        ParticipationApplication application = participationApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found: " + applicationId));

        if (!application.getOfferId().equals(offerId)) {
            throw new BadRequestException("Application does not belong to the specified offer");
        }

        return application;
    }

    private void validateReviewableApplication(UUID offerId, ParticipationApplication application) {
        if (application.getStatus() != ParticipationApplicationStatus.PENDING_ORGANIZER_REVIEW) {
            throw new ConflictException("Application is not waiting for organizer review");
        }
    }
}
