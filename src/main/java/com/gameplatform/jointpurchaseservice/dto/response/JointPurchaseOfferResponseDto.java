package com.gameplatform.jointpurchaseservice.dto.response;

import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseOfferStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationApplicationStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JointPurchaseOfferResponseDto {

    private UUID id;
    private UUID organizerUserId;
    private String title;
    private String description;
    private String allianceName;
    private String contactGroup;
    private Boolean showOrganizerContacts;
    private Boolean showOrganizerGameNickname;
    private Boolean showOrganizerTelegram;
    private Boolean showOrganizerVk;
    private Boolean showOrganizerDiscord;
    private String organizerGameNickname;
    private String organizerTelegramUsername;
    private String organizerVkUsername;
    private String organizerDiscordUsername;
    private Integer participantsEmailSendCount;
    private OffsetDateTime nextParticipantsEmailAllowedAt;
    private String screenshotBucket;
    private String screenshotObjectKey;
    private String screenshotUrl;
    private Integer requiredParticipants;
    private Integer reserveParticipants;
    private Integer currentMainParticipants;
    private Integer currentReserveParticipants;
    private ParticipationApplicationStatus currentUserApplicationStatus;
    private ParticipationType currentUserAssignedParticipationType;
    private Boolean autoApproveEnabled;
    private JointPurchaseOfferStatus status;
    private OffsetDateTime plannedStartAt;
    private OffsetDateTime plannedEndAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
