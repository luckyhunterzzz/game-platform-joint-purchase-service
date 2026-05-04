package com.gameplatform.jointpurchaseservice.domain.entity;

import com.gameplatform.jointpurchaseservice.domain.enums.ParticipantFeedbackResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "joint_purchase_participant_feedback")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JointPurchaseParticipantFeedback {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "offer_id", nullable = false)
    private UUID offerId;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "participant_user_id", nullable = false)
    private UUID participantUserId;

    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 20)
    private ParticipantFeedbackResult result;

    @Column(name = "description", length = 3000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
