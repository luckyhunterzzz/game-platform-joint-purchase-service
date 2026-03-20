package com.gameplatform.jointpurchaseservice.kafka.consumer;

import com.gameplatform.jointpurchaseservice.facade.TrustScoreProcessingFacade;
import com.gameplatform.jointpurchaseservice.kafka.event.TrustScoreCalculatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrustScoreCalculatedConsumer {

    private final TrustScoreProcessingFacade trustScoreProcessingFacade;

    @KafkaListener(
            topics = "${app.kafka.topics.trust-score-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "trustScoreCalculatedKafkaListenerContainerFactory"
    )
    public void handle(TrustScoreCalculatedEvent event) {
        log.info(
                "Received TrustScoreCalculatedEvent: eventId={}, sourceEventId={}, applicationId={}, offerId={}, userId={}, recommendation={}",
                event.getEventId(),
                event.getSourceEventId(),
                event.getApplicationId(),
                event.getOfferId(),
                event.getUserId(),
                event.getRecommendation()
        );

        trustScoreProcessingFacade.processTrustScoreCalculatedEvent(event);
    }
}