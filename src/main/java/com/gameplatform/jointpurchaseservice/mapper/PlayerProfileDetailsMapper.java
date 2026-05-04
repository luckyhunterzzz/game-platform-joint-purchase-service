package com.gameplatform.jointpurchaseservice.mapper;

import com.gameplatform.jointpurchaseservice.dto.response.PlayerProfileDetailsResponseDto;
import com.gameplatform.jointpurchaseservice.integration.playerprofile.PlayerProfileResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlayerProfileDetailsMapper {

    PlayerProfileDetailsResponseDto toResponseDto(PlayerProfileResponse source);
}
