package com.gameplatform.jointpurchaseservice.integration.playerprofile;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PlayerProfileResponse(
        UUID id,
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String telegramUsername,
        String vkUsername,
        String discordUsername,
        String currentGameNickname,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
