package com.gameplatform.jointpurchaseservice.domain.entity;

import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseParticipantStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationType;
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
@Table(name = "joint_purchase_participants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JointPurchaseParticipant {

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

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "participation_type", nullable = false)
    private ParticipationType participationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JointPurchaseParticipantStatus status;

    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
