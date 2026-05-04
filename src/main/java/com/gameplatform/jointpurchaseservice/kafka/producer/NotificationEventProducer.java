package com.gameplatform.jointpurchaseservice.kafka.producer;

import com.gameplatform.jointpurchaseservice.kafka.event.JointPurchaseParticipantsEmailRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventProducer {

    private final KafkaTemplate<String, JointPurchaseParticipantsEmailRequestedEvent> kafkaTemplate;

    @Value("${app.kafka.topics.notification-events}")
    private String notificationEventsTopic;

    public void publishJointPurchaseParticipantsEmailRequested(
            JointPurchaseParticipantsEmailRequestedEvent event
    ) {
        String key = event.getOfferId().toString();

        kafkaTemplate.send(notificationEventsTopic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error(
                                "Failed to send notification event to Kafka: topic={}, eventId={}",
                                notificationEventsTopic,
                                event.getEventId(),
                                ex
                        );
                    } else {
                        log.debug(
                                "Notification event sent successfully: topic={}, offset={}",
                                notificationEventsTopic,
                                result.getRecordMetadata().offset()
                        );
                    }
                });
    }
}
