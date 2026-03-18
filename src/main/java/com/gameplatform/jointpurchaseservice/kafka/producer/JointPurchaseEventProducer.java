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
        log.info("!!! ПОПЫТКА ОТПРАВКИ В ТОПИК: {} !!!", jointPurchaseEventsTopic);

        kafkaTemplate.send(jointPurchaseEventsTopic, event.getApplicantUserId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("!!! ПОДТВЕРЖДЕНО: Сообщение в топике {}, partition {}, offset {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("!!! ОШИБКА КАФКИ: Сообщение НЕ отправлено !!!", ex);
                    }
                });
    }
}