package com.gameplatform.jointpurchaseservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateJointPurchaseOfferRequestDto {

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    @NotBlank
    @Size(max = 150)
    private String allianceName;

    @NotBlank
    @Size(max = 150)
    private String contactGroup;

    @NotNull
    private Boolean showOrganizerContacts;

    @NotNull
    private Boolean showOrganizerGameNickname;

    @NotNull
    private Boolean showOrganizerTelegram;

    @NotNull
    private Boolean showOrganizerVk;

    @NotNull
    private Boolean showOrganizerDiscord;

    private String screenshotBucket;

    @Size(max = 1024)
    private String screenshotObjectKey;

    @NotNull
    @Min(1)
    private Integer requiredParticipants;

    @NotNull
    @Min(0)
    private Integer reserveParticipants;

    @NotNull
    private Boolean autoApproveEnabled;

    @NotNull
    @Future
    private OffsetDateTime plannedStartAt;

    @NotNull
    @Future
    private OffsetDateTime plannedEndAt;
}
