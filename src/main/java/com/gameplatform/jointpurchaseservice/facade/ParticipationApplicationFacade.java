package com.gameplatform.jointpurchaseservice.facade;

import com.gameplatform.jointpurchaseservice.domain.entity.ParticipationApplication;
import com.gameplatform.jointpurchaseservice.dto.request.SubmitParticipationApplicationRequestDto;
import com.gameplatform.jointpurchaseservice.dto.response.JointPurchaseOfferResponseDto;
import com.gameplatform.jointpurchaseservice.dto.response.ParticipationApplicationResponseDto;
import com.gameplatform.jointpurchaseservice.mapper.JointPurchaseOfferMapper;
import com.gameplatform.jointpurchaseservice.mapper.ParticipationApplicationMapper;
import com.gameplatform.jointpurchaseservice.integration.playerprofile.PlayerProfileClient;
import com.gameplatform.jointpurchaseservice.integration.playerprofile.PlayerProfileResponse;
import com.gameplatform.jointpurchaseservice.media.resolver.MediaUrlResolver;
import com.gameplatform.jointpurchaseservice.service.JointPurchaseOfferService;
import com.gameplatform.jointpurchaseservice.service.JointPurchaseNotificationService;
import com.gameplatform.jointpurchaseservice.service.ParticipationApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ParticipationApplicationFacade {

    private final ParticipationApplicationService participationApplicationService;
    private final ParticipationApplicationMapper participationApplicationMapper;
    private final JointPurchaseOfferService jointPurchaseOfferService;
    private final JointPurchaseOfferMapper jointPurchaseOfferMapper;
    private final MediaUrlResolver mediaUrlResolver;
    private final PlayerProfileClient playerProfileClient;
    private final JointPurchaseNotificationService jointPurchaseNotificationService;

    public ParticipationApplicationResponseDto submitApplication(
            UUID offerId,
            UUID applicantUserId,
            String applicantEmail,
            SubmitParticipationApplicationRequestDto requestDto
    ) {
        ParticipationApplication application =
                participationApplicationService.submitApplication(offerId, applicantUserId, applicantEmail, requestDto);

        return participationApplicationMapper.toResponseDto(application);
    }

    public ParticipationApplicationResponseDto cancelOwnApplication(UUID applicantUserId, UUID offerId) {
        ParticipationApplication application =
                participationApplicationService.cancelOwnApplication(applicantUserId, offerId);
        return participationApplicationMapper.toResponseDto(application);
    }

    public List<JointPurchaseOfferResponseDto> getOpenOffers(UUID currentUserId) {
        return jointPurchaseOfferService.getVisibleOffers(currentUserId).stream()
                .map(offer -> {
                    JointPurchaseOfferResponseDto responseDto = jointPurchaseOfferMapper.toResponseDto(offer);
                    responseDto.setScreenshotUrl(mediaUrlResolver.resolvePublicUrl(
                            offer.getScreenshotBucket(),
                            offer.getScreenshotObjectKey()
                    ));
                    if (Boolean.TRUE.equals(offer.getShowOrganizerContacts())) {
                        try {
                            PlayerProfileResponse organizerProfile =
                                    playerProfileClient.getProfileByUserId(offer.getOrganizerUserId());
                            responseDto.setOrganizerGameNickname(
                                    Boolean.TRUE.equals(offer.getShowOrganizerGameNickname()) ? organizerProfile.currentGameNickname() : null
                            );
                            responseDto.setOrganizerTelegramUsername(
                                    Boolean.TRUE.equals(offer.getShowOrganizerTelegram()) ? organizerProfile.telegramUsername() : null
                            );
                            responseDto.setOrganizerVkUsername(
                                    Boolean.TRUE.equals(offer.getShowOrganizerVk()) ? organizerProfile.vkUsername() : null
                            );
                            responseDto.setOrganizerDiscordUsername(
                                    Boolean.TRUE.equals(offer.getShowOrganizerDiscord()) ? organizerProfile.discordUsername() : null
                            );
                        } catch (RuntimeException ignored) {
                            responseDto.setOrganizerGameNickname(null);
                            responseDto.setOrganizerTelegramUsername(null);
                            responseDto.setOrganizerVkUsername(null);
                            responseDto.setOrganizerDiscordUsername(null);
                        }
                    }
                    responseDto.setParticipantsEmailSendCount(offer.getParticipantsEmailSendCount());
                    responseDto.setNextParticipantsEmailAllowedAt(
                            jointPurchaseNotificationService.getNextParticipantsEmailAllowedAt(offer)
                    );

                    if (currentUserId != null) {
                        ParticipationApplication application =
                                participationApplicationService.getCurrentUserApplication(offer.getId(), currentUserId);
                        if (application != null) {
                            responseDto.setCurrentUserApplicationStatus(application.getStatus());
                            responseDto.setCurrentUserAssignedParticipationType(application.getAssignedParticipationType());
                        }
                    }

                    return responseDto;
                })
                .toList();
    }
}
