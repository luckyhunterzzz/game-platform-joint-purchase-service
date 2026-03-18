package com.gameplatform.jointpurchaseservice.controller;

import com.gameplatform.jointpurchaseservice.dto.request.CreateJointPurchaseOfferRequestDto;
import com.gameplatform.jointpurchaseservice.dto.response.JointPurchaseOfferResponseDto;
import com.gameplatform.jointpurchaseservice.facade.JointPurchaseOfferFacade;
import com.gameplatform.jointpurchaseservice.utility.HeaderNames;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizer/joint-purchases")
@RequiredArgsConstructor
public class JointPurchaseOfferController {

    private final JointPurchaseOfferFacade jointPurchaseOfferFacade;

    @PostMapping
    public ResponseEntity<JointPurchaseOfferResponseDto> createOffer(
            @RequestHeader(HeaderNames.USER_ID) UUID organizerUserId,
            @Valid @RequestBody CreateJointPurchaseOfferRequestDto requestDto
    ) {
        JointPurchaseOfferResponseDto response = jointPurchaseOfferFacade.createOffer(organizerUserId, requestDto);
        return ResponseEntity.ok(response);
    }
}