package com.gameplatform.jointpurchaseservice.integration.playerprofile;

import com.gameplatform.jointpurchaseservice.utility.HeaderNames;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(
        name = "player-profile-client",
        url = "${app.services.player-profile.base-url}"
)
public interface PlayerProfileFeignClient {

    @GetMapping("/api/v1/profile/me")
    PlayerProfileResponse getMyProfile(
            @RequestHeader(HeaderNames.USER_ID) String userId,
            @RequestHeader(HeaderNames.USER_EMAIL) String email
    );

    @GetMapping("/api/v1/internal/player-profiles/{userId}")
    PlayerProfileResponse getProfileByUserId(@PathVariable UUID userId);
}
