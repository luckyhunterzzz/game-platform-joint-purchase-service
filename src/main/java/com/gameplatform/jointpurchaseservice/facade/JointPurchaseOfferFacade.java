package com.gameplatform.jointpurchaseservice.facade;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseOffer;
import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseParticipantFeedback;
import com.gameplatform.jointpurchaseservice.dto.request.CreateJointPurchaseOfferRequestDto;
import com.gameplatform.jointpurchaseservice.dto.request.MoveParticipationRequestDto;
import com.gameplatform.jointpurchaseservice.dto.request.ReviewParticipationApplicationRequestDto;
import com.gameplatform.jointpurchaseservice.dto.request.SendOfferParticipantsEmailRequestDto;
import com.gameplatform.jointpurchaseservice.dto.request.UpdateJointPurchaseOfferStatusRequestDto;
import com.gameplatform.jointpurchaseservice.dto.request.UpsertParticipantFeedbackRequestDto;
import com.gameplatform.jointpurchaseservice.dto.response.ApplicationScreenshotAccessResponseDto;
import com.gameplatform.jointpurchaseservice.dto.response.JointPurchaseOfferResponseDto;
import com.gameplatform.jointpurchaseservice.dto.response.OfferParticipantsEmailResponseDto;
import com.gameplatform.jointpurchaseservice.dto.response.ParticipantFeedbackResponseDto;
import com.gameplatform.jointpurchaseservice.dto.response.ParticipationApplicationDetailsResponseDto;
import com.gameplatform.jointpurchaseservice.dto.response.ParticipationApplicationResponseDto;
import com.gameplatform.jointpurchaseservice.integration.playerprofile.PlayerProfileClient;
import com.gameplatform.jointpurchaseservice.integration.playerprofile.PlayerProfileResponse;
import com.gameplatform.jointpurchaseservice.mapper.JointPurchaseOfferMapper;
import com.gameplatform.jointpurchaseservice.mapper.ParticipantFeedbackMapper;
import com.gameplatform.jointpurchaseservice.mapper.ParticipationApplicationMapper;
import com.gameplatform.jointpurchaseservice.media.resolver.MediaUrlResolver;
import com.gameplatform.jointpurchaseservice.service.JointPurchaseNotificationService;
import com.gameplatform.jointpurchaseservice.service.JointPurchaseOfferService;
import com.gameplatform.jointpurchaseservice.service.ParticipantFeedbackService;
import com.gameplatform.jointpurchaseservice.service.ParticipationApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JointPurchaseOfferFacade {

    private final JointPurchaseOfferService jointPurchaseOfferService;
    private final JointPurchaseOfferMapper jointPurchaseOfferMapper;
    private final ParticipationApplicationService participationApplicationService;
    private final ParticipationApplicationMapper participationApplicationMapper;
    private final ParticipantFeedbackService participantFeedbackService;
    private final ParticipantFeedbackMapper participantFeedbackMapper;
    private final MediaUrlResolver mediaUrlResolver;
    private final PlayerProfileClient playerProfileClient;
    private final JointPurchaseNotificationService jointPurchaseNotificationService;

    public JointPurchaseOfferResponseDto createOffer(
            UUID organizerUserId,
            String organizerEmail,
            List<String> organizerRoles,
            CreateJointPurchaseOfferRequestDto requestDto
    ) {
        JointPurchaseOffer jointPurchaseOffer = jointPurchaseOfferService.createOffer(
                organizerUserId,
                organizerEmail,
                organizerRoles,
                requestDto
        );
        return enrichOfferResponse(jointPurchaseOfferMapper.toResponseDto(jointPurchaseOffer), jointPurchaseOffer);
    }

    public List<JointPurchaseOfferResponseDto> getOrganizerOffers(UUID organizerUserId) {
        return jointPurchaseOfferService.getOrganizerOffers(organizerUserId).stream()
                .map(offer -> enrichOfferResponse(jointPurchaseOfferMapper.toResponseDto(offer), offer))
                .toList();
    }

    public JointPurchaseOfferResponseDto updateOfferStatus(
            UUID organizerUserId,
            UUID offerId,
            UpdateJointPurchaseOfferStatusRequestDto requestDto
    ) {
        JointPurchaseOffer offer = jointPurchaseOfferService.updateOfferStatus(
                organizerUserId,
                offerId,
                requestDto.getStatus()
        );

        return enrichOfferResponse(jointPurchaseOfferMapper.toResponseDto(offer), offer);
    }

    public List<ParticipationApplicationResponseDto> getOfferApplications(UUID organizerUserId, UUID offerId) {
        return participationApplicationService.getOfferApplications(organizerUserId, offerId).stream()
                .map(application -> {
                    ParticipationApplicationResponseDto responseDto =
                            participationApplicationMapper.toResponseDto(application);
                    try {
                        PlayerProfileResponse playerProfile =
                                playerProfileClient.getProfileByUserId(application.getApplicantUserId());
                        responseDto.setApplicantEmail(playerProfile.email());
                    } catch (RuntimeException ignored) {
                        responseDto.setApplicantEmail(null);
                    }
                    return responseDto;
                })
                .toList();
    }

    public OfferParticipantsEmailResponseDto sendOfferParticipantsEmail(
            UUID organizerUserId,
            String organizerEmail,
            UUID offerId,
            SendOfferParticipantsEmailRequestDto requestDto
    ) {
        return jointPurchaseNotificationService.sendOfferParticipantsEmail(
                organizerUserId,
                organizerEmail,
                offerId,
                requestDto
        );
    }

    public ParticipationApplicationResponseDto approveApplication(
            UUID organizerUserId,
            UUID offerId,
            UUID applicationId,
            ReviewParticipationApplicationRequestDto requestDto
    ) {
        return participationApplicationMapper.toResponseDto(
                participationApplicationService.approveApplication(
                        organizerUserId,
                        offerId,
                        applicationId,
                        requestDto.getParticipationType()
                )
        );
    }

    public ParticipationApplicationResponseDto rejectApplication(
            UUID organizerUserId,
            UUID offerId,
            UUID applicationId
    ) {
        return participationApplicationMapper.toResponseDto(
                participationApplicationService.rejectApplication(organizerUserId, offerId, applicationId)
        );
    }

    public ParticipationApplicationDetailsResponseDto getApplicationDetails(
            UUID organizerUserId,
            UUID offerId,
            UUID applicationId
    ) {
        return participationApplicationService.getApplicationDetails(organizerUserId, offerId, applicationId);
    }

    public ApplicationScreenshotAccessResponseDto getApplicationScreenshotAccess(
            UUID organizerUserId,
            UUID offerId,
            UUID applicationId
    ) {
        return ApplicationScreenshotAccessResponseDto.builder()
                .url(participationApplicationService.getApplicationScreenshotAccessUrl(
                        organizerUserId,
                        offerId,
                        applicationId
                ))
                .build();
    }

    public ParticipationApplicationResponseDto moveApprovedApplication(
            UUID organizerUserId,
            UUID offerId,
            UUID applicationId,
            MoveParticipationRequestDto requestDto
    ) {
        return participationApplicationMapper.toResponseDto(
                participationApplicationService.moveApprovedApplication(
                        organizerUserId,
                        offerId,
                        applicationId,
                        requestDto.getParticipationType()
                )
        );
    }

    public List<ParticipantFeedbackResponseDto> getOfferFeedback(UUID organizerUserId, UUID offerId) {
        return participantFeedbackService.getOfferFeedback(organizerUserId, offerId).stream()
                .map(participantFeedbackMapper::toResponseDto)
                .toList();
    }

    public ParticipantFeedbackResponseDto upsertParticipantFeedback(
            UUID organizerUserId,
            UUID offerId,
            UUID applicationId,
            UpsertParticipantFeedbackRequestDto requestDto
    ) {
        JointPurchaseParticipantFeedback feedback = participantFeedbackService.upsertFeedback(
                organizerUserId,
                offerId,
                applicationId,
                requestDto
        );

        return participantFeedbackMapper.toResponseDto(feedback);
    }

    private JointPurchaseOfferResponseDto enrichOfferResponse(
            JointPurchaseOfferResponseDto responseDto,
            JointPurchaseOffer offer
    ) {
        responseDto.setScreenshotUrl(mediaUrlResolver.resolvePublicUrl(
                offer.getScreenshotBucket(),
                offer.getScreenshotObjectKey()
        ));
        return responseDto;
    }
}
