package com.gameplatform.jointpurchaseservice.controller;

import com.gameplatform.jointpurchaseservice.dto.request.CreateJointPurchaseOfferRequestDto;
import com.gameplatform.jointpurchaseservice.dto.request.MoveParticipationRequestDto;
import com.gameplatform.jointpurchaseservice.dto.request.ReviewParticipationApplicationRequestDto;
import com.gameplatform.jointpurchaseservice.dto.request.SendOfferParticipantsEmailRequestDto;
import com.gameplatform.jointpurchaseservice.dto.request.UpdateJointPurchaseOfferStatusRequestDto;
import com.gameplatform.jointpurchaseservice.dto.request.UpsertParticipantFeedbackRequestDto;
import com.gameplatform.jointpurchaseservice.dto.response.ApplicationScreenshotAccessResponseDto;
import com.gameplatform.jointpurchaseservice.dto.response.JointPurchaseOfferResponseDto;
import com.gameplatform.jointpurchaseservice.dto.response.OfferParticipantsEmailResponseDto;
import com.gameplatform.jointpurchaseservice.dto.response.ParticipantFeedbackResponseDto;
import com.gameplatform.jointpurchaseservice.dto.response.ParticipationApplicationDetailsResponseDto;
import com.gameplatform.jointpurchaseservice.dto.response.ParticipationApplicationResponseDto;
import com.gameplatform.jointpurchaseservice.facade.JointPurchaseOfferFacade;
import com.gameplatform.jointpurchaseservice.utility.HeaderNames;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizer/joint-purchases")
@RequiredArgsConstructor
public class JointPurchaseOfferController {

    private final JointPurchaseOfferFacade jointPurchaseOfferFacade;

    @PostMapping
    public ResponseEntity<JointPurchaseOfferResponseDto> createOffer(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId,
            @RequestHeader(HeaderNames.USER_EMAIL) String organizerEmail,
            @RequestHeader(HeaderNames.USER_ROLES) String organizerRoles,
            @Valid @RequestBody CreateJointPurchaseOfferRequestDto requestDto
    ) {
        JointPurchaseOfferResponseDto response = jointPurchaseOfferFacade.createOffer(
                organizerUserId,
                organizerEmail,
                parseRoles(organizerRoles),
                requestDto
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<JointPurchaseOfferResponseDto>> getOrganizerOffers(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId
    ) {
        return ResponseEntity.ok(jointPurchaseOfferFacade.getOrganizerOffers(organizerUserId));
    }

    @PutMapping("/{offerId}")
    public ResponseEntity<JointPurchaseOfferResponseDto> updateOffer(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId,
            @PathVariable UUID offerId,
            @Valid @RequestBody CreateJointPurchaseOfferRequestDto requestDto
    ) {
        return ResponseEntity.ok(jointPurchaseOfferFacade.updateOffer(organizerUserId, offerId, requestDto));
    }

    @PatchMapping("/{offerId}/status")
    public ResponseEntity<JointPurchaseOfferResponseDto> updateOfferStatus(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId,
            @PathVariable UUID offerId,
            @Valid @RequestBody UpdateJointPurchaseOfferStatusRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                jointPurchaseOfferFacade.updateOfferStatus(organizerUserId, offerId, requestDto)
        );
    }

    @GetMapping("/{offerId}/applications")
    public ResponseEntity<List<ParticipationApplicationResponseDto>> getOfferApplications(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId,
            @PathVariable UUID offerId
    ) {
        return ResponseEntity.ok(jointPurchaseOfferFacade.getOfferApplications(organizerUserId, offerId));
    }

    @PostMapping("/{offerId}/notifications/email")
    public ResponseEntity<OfferParticipantsEmailResponseDto> sendOfferParticipantsEmail(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId,
            @RequestHeader(HeaderNames.USER_EMAIL) String organizerEmail,
            @PathVariable UUID offerId,
            @Valid @RequestBody SendOfferParticipantsEmailRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                jointPurchaseOfferFacade.sendOfferParticipantsEmail(
                        organizerUserId,
                        organizerEmail,
                        offerId,
                        requestDto
                )
        );
    }

    @GetMapping("/{offerId}/applications/{applicationId}")
    public ResponseEntity<ParticipationApplicationDetailsResponseDto> getApplicationDetails(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId,
            @PathVariable UUID offerId,
            @PathVariable UUID applicationId
    ) {
        return ResponseEntity.ok(
                jointPurchaseOfferFacade.getApplicationDetails(organizerUserId, offerId, applicationId)
        );
    }

    @GetMapping("/{offerId}/applications/{applicationId}/screenshot")
    public ResponseEntity<ApplicationScreenshotAccessResponseDto> getApplicationScreenshotAccess(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId,
            @PathVariable UUID offerId,
            @PathVariable UUID applicationId
    ) {
        return ResponseEntity.ok(
                jointPurchaseOfferFacade.getApplicationScreenshotAccess(organizerUserId, offerId, applicationId)
        );
    }

    @PostMapping("/{offerId}/applications/{applicationId}/approve")
    public ResponseEntity<ParticipationApplicationResponseDto> approveApplication(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId,
            @PathVariable UUID offerId,
            @PathVariable UUID applicationId,
            @Valid @RequestBody ReviewParticipationApplicationRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                jointPurchaseOfferFacade.approveApplication(
                        organizerUserId,
                        offerId,
                        applicationId,
                        requestDto
                )
        );
    }

    @PostMapping("/{offerId}/applications/{applicationId}/reject")
    public ResponseEntity<ParticipationApplicationResponseDto> rejectApplication(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId,
            @PathVariable UUID offerId,
            @PathVariable UUID applicationId
    ) {
        return ResponseEntity.ok(
                jointPurchaseOfferFacade.rejectApplication(organizerUserId, offerId, applicationId)
        );
    }

    @PatchMapping("/{offerId}/applications/{applicationId}/participation")
    public ResponseEntity<ParticipationApplicationResponseDto> moveApprovedApplication(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId,
            @PathVariable UUID offerId,
            @PathVariable UUID applicationId,
            @Valid @RequestBody MoveParticipationRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                jointPurchaseOfferFacade.moveApprovedApplication(
                        organizerUserId,
                        offerId,
                        applicationId,
                        requestDto
                )
        );
    }

    @DeleteMapping("/{offerId}/applications/{applicationId}")
    public ResponseEntity<ParticipationApplicationResponseDto> cancelApprovedApplication(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId,
            @PathVariable UUID offerId,
            @PathVariable UUID applicationId
    ) {
        return ResponseEntity.ok(
                jointPurchaseOfferFacade.cancelApprovedApplication(organizerUserId, offerId, applicationId)
        );
    }

    @GetMapping("/{offerId}/feedback")
    public ResponseEntity<List<ParticipantFeedbackResponseDto>> getOfferFeedback(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId,
            @PathVariable UUID offerId
    ) {
        return ResponseEntity.ok(jointPurchaseOfferFacade.getOfferFeedback(organizerUserId, offerId));
    }

    @PutMapping("/{offerId}/applications/{applicationId}/feedback")
    public ResponseEntity<ParticipantFeedbackResponseDto> upsertParticipantFeedback(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId,
            @PathVariable UUID offerId,
            @PathVariable UUID applicationId,
            @Valid @RequestBody UpsertParticipantFeedbackRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                jointPurchaseOfferFacade.upsertParticipantFeedback(
                        organizerUserId,
                        offerId,
                        applicationId,
                        requestDto
                )
        );
    }

    private List<String> parseRoles(String rolesHeader) {
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .toList();
    }
}
