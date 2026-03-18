package com.gameplatform.jointpurchaseservice.mapper;

import com.gameplatform.jointpurchaseservice.domain.entity.ParticipationApplication;
import com.gameplatform.jointpurchaseservice.dto.response.ParticipationApplicationResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParticipationApplicationMapper {

    ParticipationApplicationResponseDto toResponseDto(ParticipationApplication application);
}