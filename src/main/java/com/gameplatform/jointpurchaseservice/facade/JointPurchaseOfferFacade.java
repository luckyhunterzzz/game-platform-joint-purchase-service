package com.gameplatform.jointpurchaseservice.facade;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseOffer;
import com.gameplatform.jointpurchaseservice.dto.request.CreateJointPurchaseOfferRequestDto;
import com.gameplatform.jointpurchaseservice.dto.response.JointPurchaseOfferResponseDto;
import com.gameplatform.jointpurchaseservice.mapper.JointPurchaseOfferMapper;
import com.gameplatform.jointpurchaseservice.service.JointPurchaseOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JointPurchaseOfferFacade {

    private final JointPurchaseOfferService jointPurchaseOfferService;
    private final JointPurchaseOfferMapper jointPurchaseOfferMapper;

    public JointPurchaseOfferResponseDto createOffer(UUID organizerUserId, CreateJointPurchaseOfferRequestDto requestDto) {
        JointPurchaseOffer jointPurchaseOffer = jointPurchaseOfferService.createOffer(organizerUserId, requestDto);
        return jointPurchaseOfferMapper.toResponseDto(jointPurchaseOffer);
    }
}