package com.gameplatform.jointpurchaseservice.domain.entity;

import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseOfferStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "joint_purchase_offers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JointPurchaseOffer {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "organizer_user_id", nullable = false)
    private UUID organizerUserId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "alliance_name", nullable = false)
    private String allianceName;

    @Column(name = "required_participants", nullable = false)
    private Integer requiredParticipants;

    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JointPurchaseOfferStatus status;

    @Column(name = "purchase_window_start", nullable = false)
    private OffsetDateTime purchaseWindowStart;

    @Column(name = "purchase_window_end", nullable = false)
    private OffsetDateTime purchaseWindowEnd;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}