package com.gameplatform.jointpurchaseservice.mapper;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseParticipantFeedback;
import com.gameplatform.jointpurchaseservice.dto.response.ParticipantFeedbackResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParticipantFeedbackMapper {

    ParticipantFeedbackResponseDto toResponseDto(JointPurchaseParticipantFeedback feedback);
}
