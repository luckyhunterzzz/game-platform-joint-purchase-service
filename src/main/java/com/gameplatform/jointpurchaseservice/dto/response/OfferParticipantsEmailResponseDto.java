package com.gameplatform.jointpurchaseservice.dto.response;

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
public class OfferParticipantsEmailResponseDto {
    private UUID eventId;
    private UUID offerId;
    private Integer recipientsCount;
    private OffsetDateTime requestedAt;
}
