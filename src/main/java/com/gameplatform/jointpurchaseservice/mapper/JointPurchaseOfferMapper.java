package com.gameplatform.jointpurchaseservice.mapper;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseOffer;
import com.gameplatform.jointpurchaseservice.dto.response.JointPurchaseOfferResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JointPurchaseOfferMapper {

    JointPurchaseOfferResponseDto toResponseDto(JointPurchaseOffer jointPurchaseOffer);
}