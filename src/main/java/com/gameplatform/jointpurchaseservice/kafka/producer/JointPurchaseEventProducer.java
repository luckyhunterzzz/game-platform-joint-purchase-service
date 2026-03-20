package com.gameplatform.jointpurchaseservice.kafka.producer;

import com.gameplatform.jointpurchaseservice.kafka.event.ParticipationApplicationSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JointPurchaseEventProducer {

    private final KafkaTemplate<String, ParticipationApplicationSubmittedEvent> kafkaTemplate;

    @Value("${app.kafka.topics.joint-purchase-events}")
    private String jointPurchaseEventsTopic;

    public void publishParticipationApplicationSubmitted(ParticipationApplicationSubmittedEvent event) {
        String key = event.getApplicantUserId().toString();

        kafkaTemplate.send(jointPurchaseEventsTopic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send event to Kafka: topic={}, eventId={}",
                                jointPurchaseEventsTopic, event.getEventId(), ex);
                    } else {
                        log.debug("Event sent successfully: topic={}, offset={}",
                                jointPurchaseEventsTopic, result.getRecordMetadata().offset());
                    }
                });
    }
}