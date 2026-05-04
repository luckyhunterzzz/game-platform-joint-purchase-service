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
public class PlayerProfileDetailsResponseDto {
    private UUID id;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String telegramUsername;
    private String vkUsername;
    private String discordUsername;
    private String currentGameNickname;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
