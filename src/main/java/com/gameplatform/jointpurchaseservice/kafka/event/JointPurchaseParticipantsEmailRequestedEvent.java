package com.gameplatform.jointpurchaseservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JointPurchaseParticipantsEmailRequestedEvent {
    private UUID eventId;
    private OffsetDateTime occurredAt;
    private UUID offerId;
    private UUID organizerUserId;
    private String organizerEmail;
    private String offerTitle;
    private String subject;
    private String message;
    private List<EmailNotificationRecipientEvent> recipients;
}
