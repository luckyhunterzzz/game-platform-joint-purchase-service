package com.gameplatform.jointpurchaseservice.facade;

import com.gameplatform.jointpurchaseservice.kafka.event.TrustScoreCalculatedEvent;
import com.gameplatform.jointpurchaseservice.service.TrustScoreProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrustScoreProcessingFacade {

    private final TrustScoreProcessingService trustScoreProcessingService;

    public void processTrustScoreCalculatedEvent(TrustScoreCalculatedEvent event) {
        trustScoreProcessingService.processTrustScoreCalculatedEvent(event);
    }
}