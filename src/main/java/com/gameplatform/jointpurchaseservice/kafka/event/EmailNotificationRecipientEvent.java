package com.gameplatform.jointpurchaseservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationRecipientEvent {
    private UUID userId;
    private String email;
    private String displayName;
    private String participationType;
}
