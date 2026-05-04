package com.gameplatform.jointpurchaseservice.service;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseOffer;
import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseParticipant;
import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseParticipantStatus;
import com.gameplatform.jointpurchaseservice.dto.request.SendOfferParticipantsEmailRequestDto;
import com.gameplatform.jointpurchaseservice.dto.response.OfferParticipantsEmailResponseDto;
import com.gameplatform.jointpurchaseservice.exception.BadRequestException;
import com.gameplatform.jointpurchaseservice.exception.ForbiddenException;
import com.gameplatform.jointpurchaseservice.integration.playerprofile.PlayerProfileClient;
import com.gameplatform.jointpurchaseservice.integration.playerprofile.PlayerProfileResponse;
import com.gameplatform.jointpurchaseservice.kafka.event.EmailNotificationRecipientEvent;
import com.gameplatform.jointpurchaseservice.kafka.event.JointPurchaseParticipantsEmailRequestedEvent;
import com.gameplatform.jointpurchaseservice.kafka.producer.NotificationEventProducer;
import com.gameplatform.jointpurchaseservice.repository.jpa.JointPurchaseParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JointPurchaseNotificationService {

    private final JointPurchaseOfferService jointPurchaseOfferService;
    private final JointPurchaseParticipantRepository jointPurchaseParticipantRepository;
    private final PlayerProfileClient playerProfileClient;
    private final NotificationEventProducer notificationEventProducer;
    private final Clock clock;

    @Transactional(readOnly = true)
    public OfferParticipantsEmailResponseDto sendOfferParticipantsEmail(
            UUID organizerUserId,
            String organizerEmail,
            UUID offerId,
            SendOfferParticipantsEmailRequestDto requestDto
    ) {
        JointPurchaseOffer offer = jointPurchaseOfferService.getOfferOrThrow(offerId);

        if (!offer.getOrganizerUserId().equals(organizerUserId)) {
            throw new ForbiddenException("Only the creator can notify offer participants");
        }

        List<JointPurchaseParticipant> participants =
                jointPurchaseParticipantRepository.findAllByOfferIdAndStatusOrderByJoinedAtAsc(
                        offerId,
                        JointPurchaseParticipantStatus.ACTIVE
                );

        if (participants.isEmpty()) {
            throw new BadRequestException("There are no active participants to notify");
        }

        List<EmailNotificationRecipientEvent> recipients = new ArrayList<>();

        for (JointPurchaseParticipant participant : participants) {
            PlayerProfileResponse profile = playerProfileClient.getProfileByUserId(participant.getUserId());

            if (profile.email() == null || profile.email().isBlank()) {
                continue;
            }

            String displayName = resolveDisplayName(profile);

            recipients.add(EmailNotificationRecipientEvent.builder()
                    .userId(participant.getUserId())
                    .email(profile.email())
                    .displayName(displayName)
                    .participationType(participant.getParticipationType().name())
                    .build());
        }

        if (recipients.isEmpty()) {
            throw new BadRequestException("No participant emails are available for delivery");
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        UUID eventId = UUID.randomUUID();

        JointPurchaseParticipantsEmailRequestedEvent event =
                JointPurchaseParticipantsEmailRequestedEvent.builder()
                        .eventId(eventId)
                        .occurredAt(now)
                        .offerId(offer.getId())
                        .organizerUserId(organizerUserId)
                        .organizerEmail(organizerEmail)
                        .offerTitle(offer.getTitle())
                        .subject(requestDto.getSubject().trim())
                        .message(requestDto.getMessage().trim())
                        .recipients(recipients)
                        .build();

        notificationEventProducer.publishJointPurchaseParticipantsEmailRequested(event);

        return OfferParticipantsEmailResponseDto.builder()
                .eventId(eventId)
                .offerId(offer.getId())
                .recipientsCount(recipients.size())
                .requestedAt(now)
                .build();
    }

    private String resolveDisplayName(PlayerProfileResponse profile) {
        if (profile.currentGameNickname() != null && !profile.currentGameNickname().isBlank()) {
            return profile.currentGameNickname().trim();
        }

        if (profile.firstName() != null && !profile.firstName().isBlank()) {
            return profile.firstName().trim();
        }

        return null;
    }
}
