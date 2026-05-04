package com.gameplatform.jointpurchaseservice.integration.playerprofile;

import com.gameplatform.jointpurchaseservice.exception.BadRequestException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlayerProfileClient {

    private final PlayerProfileFeignClient playerProfileFeignClient;

    public boolean isProfileComplete(UUID userId, String email) {
        try {
            PlayerProfileResponse profile = playerProfileFeignClient.getMyProfile(userId.toString(), email);
            return profile != null && "COMPLETE".equals(profile.status());
        } catch (FeignException exception) {
            throw new BadRequestException("Unable to validate player profile status");
        }
    }

    public PlayerProfileResponse getProfileByUserId(UUID userId) {
        try {
            return playerProfileFeignClient.getProfileByUserId(userId);
        } catch (FeignException exception) {
            throw new BadRequestException("Unable to load player profile");
        }
    }
}
