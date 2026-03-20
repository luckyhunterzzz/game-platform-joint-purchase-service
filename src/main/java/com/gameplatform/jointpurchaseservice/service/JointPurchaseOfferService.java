package com.gameplatform.jointpurchaseservice.service;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseOffer;
import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseOfferStatus;
import com.gameplatform.jointpurchaseservice.dto.request.CreateJointPurchaseOfferRequestDto;
import com.gameplatform.jointpurchaseservice.repository.jpa.JointPurchaseOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JointPurchaseOfferService {

    private final JointPurchaseOfferRepository jointPurchaseOfferRepository;
    private final Clock clock;

    @Transactional
    public JointPurchaseOffer createOffer(UUID organizerUserId, CreateJointPurchaseOfferRequestDto requestDto) {
        validatePurchaseWindow(requestDto);

        OffsetDateTime now = OffsetDateTime.now(clock);

        JointPurchaseOffer jointPurchaseOffer = JointPurchaseOffer.builder()
                .id(UUID.randomUUID())
                .organizerUserId(organizerUserId)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .allianceName(requestDto.getAllianceName())
                .requiredParticipants(requestDto.getRequiredParticipants())
                .currentParticipants(0)
                .status(JointPurchaseOfferStatus.OPEN)
                .purchaseWindowStart(requestDto.getPurchaseWindowStart())
                .purchaseWindowEnd(requestDto.getPurchaseWindowEnd())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return jointPurchaseOfferRepository.save(jointPurchaseOffer);
    }

    private void validatePurchaseWindow(CreateJointPurchaseOfferRequestDto requestDto) {
        if (requestDto.getPurchaseWindowEnd().isBefore(requestDto.getPurchaseWindowStart())
                || requestDto.getPurchaseWindowEnd().isEqual(requestDto.getPurchaseWindowStart())) {
            throw new IllegalArgumentException("purchaseWindowEnd must be after purchaseWindowStart");
        }
    }
}