package com.gameplatform.jointpurchaseservice.dto.response;

import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseOfferStatus;
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
    private Integer requiredParticipants;
    private Integer currentParticipants;
    private JointPurchaseOfferStatus status;
    private OffsetDateTime purchaseWindowStart;
    private OffsetDateTime purchaseWindowEnd;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}