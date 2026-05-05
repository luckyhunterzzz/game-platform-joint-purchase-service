package com.gameplatform.jointpurchaseservice.service;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseOffer;
import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseParticipant;
import com.gameplatform.jointpurchaseservice.domain.entity.ParticipationApplication;
import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseOfferStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseParticipantStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationApplicationStatus;
import com.gameplatform.jointpurchaseservice.dto.request.CreateJointPurchaseOfferRequestDto;
import com.gameplatform.jointpurchaseservice.exception.BadRequestException;
import com.gameplatform.jointpurchaseservice.exception.ConflictException;
import com.gameplatform.jointpurchaseservice.exception.ForbiddenException;
import com.gameplatform.jointpurchaseservice.exception.NotFoundException;
import com.gameplatform.jointpurchaseservice.integration.playerprofile.PlayerProfileClient;
import com.gameplatform.jointpurchaseservice.integration.playerprofile.PlayerProfileResponse;
import com.gameplatform.jointpurchaseservice.repository.jpa.JointPurchaseParticipantRepository;
import com.gameplatform.jointpurchaseservice.repository.jpa.JointPurchaseOfferRepository;
import com.gameplatform.jointpurchaseservice.repository.jpa.ParticipationApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    private final ParticipationApplicationRepository participationApplicationRepository;
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
        validateOrganizerContactPreferences(organizerUserId, requestDto);

        OffsetDateTime now = OffsetDateTime.now(clock);

        JointPurchaseOffer jointPurchaseOffer = JointPurchaseOffer.builder()
                .id(UUID.randomUUID())
                .organizerUserId(organizerUserId)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .allianceName(requestDto.getAllianceName())
                .contactGroup(requestDto.getContactGroup())
                .showOrganizerContacts(requestDto.getShowOrganizerContacts())
                .showOrganizerGameNickname(requestDto.getShowOrganizerContacts() && requestDto.getShowOrganizerGameNickname())
                .showOrganizerTelegram(requestDto.getShowOrganizerContacts() && requestDto.getShowOrganizerTelegram())
                .showOrganizerVk(requestDto.getShowOrganizerContacts() && requestDto.getShowOrganizerVk())
                .showOrganizerDiscord(requestDto.getShowOrganizerContacts() && requestDto.getShowOrganizerDiscord())
                .participantsEmailSendCount(0)
                .lastParticipantsEmailSentAt(null)
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
        return jointPurchaseOfferRepository.findAllByStatusInOrderByCreatedAtDesc(
                List.of(
                        JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS,
                        JointPurchaseOfferStatus.MAIN_GROUP_FILLED
                )
        ).stream()
                .filter(offer ->
                        offer.getStatus() == JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS
                                || offer.getCurrentReserveParticipants() < offer.getReserveParticipants()
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JointPurchaseOffer> getVisibleOffers(UUID currentUserId) {
        List<JointPurchaseOffer> openOffers = getOpenOffers();
        if (currentUserId == null) {
            return openOffers;
        }

        List<ParticipationApplication> approvedApplications =
                participationApplicationRepository.findAllByApplicantUserIdAndStatusIn(
                        currentUserId,
                        List.of(
                                ParticipationApplicationStatus.APPROVED_MAIN,
                                ParticipationApplicationStatus.APPROVED_RESERVE
                        )
                );

        if (approvedApplications.isEmpty()) {
            return openOffers;
        }

        List<UUID> participantOfferIds = approvedApplications.stream()
                .map(ParticipationApplication::getOfferId)
                .distinct()
                .toList();

        List<JointPurchaseOffer> participantOffers =
                jointPurchaseOfferRepository.findAllByIdInOrderByCreatedAtDesc(participantOfferIds);

        Map<UUID, JointPurchaseOffer> offersById = new LinkedHashMap<>();
        openOffers.forEach(offer -> offersById.put(offer.getId(), offer));
        participantOffers.forEach(offer -> offersById.put(offer.getId(), offer));

        return offersById.values().stream()
                .sorted(Comparator.comparing(JointPurchaseOffer::getCreatedAt).reversed())
                .toList();
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

    @Transactional
    public JointPurchaseOffer updateOffer(
            UUID organizerUserId,
            UUID offerId,
            CreateJointPurchaseOfferRequestDto requestDto
    ) {
        JointPurchaseOffer offer = getOfferOrThrow(offerId);

        if (!offer.getOrganizerUserId().equals(organizerUserId)) {
            throw new ForbiddenException("Only the creator can edit the offer");
        }

        if (offer.getStatus() != JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS) {
            throw new ConflictException("Offer can be edited only while applications are open");
        }

        validatePlannedWindow(requestDto);
        validateOrganizerContactPreferences(organizerUserId, requestDto);

        if (requestDto.getRequiredParticipants() < offer.getCurrentMainParticipants()) {
            throw new BadRequestException("requiredParticipants cannot be lower than current main participants");
        }

        if (requestDto.getReserveParticipants() < offer.getCurrentReserveParticipants()) {
            throw new BadRequestException("reserveParticipants cannot be lower than current reserve participants");
        }

        offer.setTitle(requestDto.getTitle());
        offer.setDescription(requestDto.getDescription());
        offer.setAllianceName(requestDto.getAllianceName());
        offer.setContactGroup(requestDto.getContactGroup());
        offer.setShowOrganizerContacts(requestDto.getShowOrganizerContacts());
        offer.setShowOrganizerGameNickname(requestDto.getShowOrganizerContacts() && requestDto.getShowOrganizerGameNickname());
        offer.setShowOrganizerTelegram(requestDto.getShowOrganizerContacts() && requestDto.getShowOrganizerTelegram());
        offer.setShowOrganizerVk(requestDto.getShowOrganizerContacts() && requestDto.getShowOrganizerVk());
        offer.setShowOrganizerDiscord(requestDto.getShowOrganizerContacts() && requestDto.getShowOrganizerDiscord());
        offer.setScreenshotBucket(requestDto.getScreenshotBucket());
        offer.setScreenshotObjectKey(requestDto.getScreenshotObjectKey());
        offer.setRequiredParticipants(requestDto.getRequiredParticipants());
        offer.setReserveParticipants(requestDto.getReserveParticipants());
        offer.setAutoApproveEnabled(requestDto.getAutoApproveEnabled());
        offer.setPlannedStartAt(requestDto.getPlannedStartAt());
        offer.setPlannedEndAt(requestDto.getPlannedEndAt());
        offer.setUpdatedAt(OffsetDateTime.now(clock));

        return jointPurchaseOfferRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public JointPurchaseOffer getOfferOrThrow(UUID offerId) {
        return jointPurchaseOfferRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer not found: " + offerId));
    }

    @Transactional
    public void autoCancelExpiredOffers() {
        OffsetDateTime cutoff = OffsetDateTime.now(clock).minusHours(12);
        List<JointPurchaseOffer> expiredOffers =
                jointPurchaseOfferRepository.findAllByStatusInAndPlannedEndAtBeforeOrderByPlannedEndAtAsc(
                        List.of(
                                JointPurchaseOfferStatus.OPEN_FOR_APPLICATIONS,
                                JointPurchaseOfferStatus.MAIN_GROUP_FILLED,
                                JointPurchaseOfferStatus.READY_TO_START,
                                JointPurchaseOfferStatus.IN_PROGRESS
                        ),
                        cutoff
                );

        if (expiredOffers.isEmpty()) {
            return;
        }

        expiredOffers.forEach(offer -> {
            completeOrReleaseParticipantsIfNeeded(offer, JointPurchaseOfferStatus.CANCELLED);
            offer.setStatus(JointPurchaseOfferStatus.CANCELLED);
            offer.setUpdatedAt(OffsetDateTime.now(clock));
        });

        jointPurchaseOfferRepository.saveAll(expiredOffers);
    }

    private void validatePlannedWindow(CreateJointPurchaseOfferRequestDto requestDto) {
        OffsetDateTime now = OffsetDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES);

        if (!requestDto.getPlannedStartAt().isAfter(now)) {
            throw new BadRequestException("plannedStartAt must be in the future");
        }

        if (requestDto.getPlannedStartAt().isAfter(now.plusHours(48))) {
            throw new BadRequestException("plannedStartAt cannot be later than 48 hours from now");
        }

        if (requestDto.getPlannedEndAt().isBefore(requestDto.getPlannedStartAt())
                || requestDto.getPlannedEndAt().isEqual(requestDto.getPlannedStartAt())) {
            throw new BadRequestException("plannedEndAt must be after plannedStartAt");
        }

        if (requestDto.getPlannedEndAt().isAfter(requestDto.getPlannedStartAt().plusHours(48))) {
            throw new BadRequestException("plannedEndAt cannot be later than 48 hours after plannedStartAt");
        }
    }

    private void validateOrganizerContactPreferences(
            UUID organizerUserId,
            CreateJointPurchaseOfferRequestDto requestDto
    ) {
        if (!Boolean.TRUE.equals(requestDto.getShowOrganizerContacts())) {
            return;
        }

        boolean hasSelectedAnyContact =
                Boolean.TRUE.equals(requestDto.getShowOrganizerGameNickname())
                        || Boolean.TRUE.equals(requestDto.getShowOrganizerTelegram())
                        || Boolean.TRUE.equals(requestDto.getShowOrganizerVk())
                        || Boolean.TRUE.equals(requestDto.getShowOrganizerDiscord());

        if (!hasSelectedAnyContact) {
            throw new BadRequestException("At least one organizer contact field must be selected");
        }

        PlayerProfileResponse organizerProfile = playerProfileClient.getProfileByUserId(organizerUserId);

        if (Boolean.TRUE.equals(requestDto.getShowOrganizerGameNickname())
                && (organizerProfile.currentGameNickname() == null || organizerProfile.currentGameNickname().isBlank())) {
            throw new BadRequestException("Organizer game nickname is not available");
        }

        if (Boolean.TRUE.equals(requestDto.getShowOrganizerTelegram())
                && (organizerProfile.telegramUsername() == null || organizerProfile.telegramUsername().isBlank())) {
            throw new BadRequestException("Organizer Telegram username is not available");
        }

        if (Boolean.TRUE.equals(requestDto.getShowOrganizerVk())
                && (organizerProfile.vkUsername() == null || organizerProfile.vkUsername().isBlank())) {
            throw new BadRequestException("Organizer VK username is not available");
        }

        if (Boolean.TRUE.equals(requestDto.getShowOrganizerDiscord())
                && (organizerProfile.discordUsername() == null || organizerProfile.discordUsername().isBlank())) {
            throw new BadRequestException("Organizer Discord username is not available");
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
