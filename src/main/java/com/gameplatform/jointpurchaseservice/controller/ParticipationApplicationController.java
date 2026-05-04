package com.gameplatform.jointpurchaseservice.controller;

import com.gameplatform.jointpurchaseservice.dto.request.SubmitParticipationApplicationRequestDto;
import com.gameplatform.jointpurchaseservice.dto.response.JointPurchaseOfferResponseDto;
import com.gameplatform.jointpurchaseservice.dto.response.ParticipationApplicationResponseDto;
import com.gameplatform.jointpurchaseservice.facade.ParticipationApplicationFacade;
import com.gameplatform.jointpurchaseservice.utility.HeaderNames;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/joint-purchases")
@RequiredArgsConstructor
public class ParticipationApplicationController {

    private final ParticipationApplicationFacade participationApplicationFacade;

    @GetMapping
    public ResponseEntity<List<JointPurchaseOfferResponseDto>> getOpenOffers(
            @RequestHeader(value = HeaderNames.USER_ID, required = false) UUID currentUserId
    ) {
        return ResponseEntity.ok(participationApplicationFacade.getOpenOffers(currentUserId));
    }

    @PostMapping("/{offerId}/applications")
    public ResponseEntity<ParticipationApplicationResponseDto> submitApplication(
            @PathVariable UUID offerId,
            @RequestHeader(HeaderNames.USER_ID) UUID applicantUserId,
            @RequestHeader(HeaderNames.USER_EMAIL) String applicantEmail,
            @Valid @RequestBody SubmitParticipationApplicationRequestDto requestDto
    ) {
        ParticipationApplicationResponseDto response =
                participationApplicationFacade.submitApplication(offerId, applicantUserId, applicantEmail, requestDto);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{offerId}/applications/me")
    public ResponseEntity<ParticipationApplicationResponseDto> cancelOwnApplication(
            @PathVariable UUID offerId,
            @RequestHeader(HeaderNames.USER_ID) UUID applicantUserId
    ) {
        return ResponseEntity.ok(
                participationApplicationFacade.cancelOwnApplication(applicantUserId, offerId)
        );
    }
}
