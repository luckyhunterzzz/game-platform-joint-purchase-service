package com.gameplatform.jointpurchaseservice.controller;

import com.gameplatform.jointpurchaseservice.dto.response.ParticipationApplicationResponseDto;
import com.gameplatform.jointpurchaseservice.facade.ParticipationApplicationFacade;
import com.gameplatform.jointpurchaseservice.utility.HeaderNames;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/joint-purchases")
@RequiredArgsConstructor
public class ParticipationApplicationController {

    private final ParticipationApplicationFacade participationApplicationFacade;

    @PostMapping("/{offerId}/applications")
    public ResponseEntity<ParticipationApplicationResponseDto> submitApplication(
            @PathVariable UUID offerId,
            @RequestHeader(HeaderNames.USER_ID) UUID applicantUserId
    ) {
        ParticipationApplicationResponseDto response =
                participationApplicationFacade.submitApplication(offerId, applicantUserId);

        return ResponseEntity.ok(response);
    }
}