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

    @Column(name = "alliance_name")
    private String allianceName;

    @Column(name = "required_participants", nullable = false)
    private Integer requiredParticipants;

    @Column(name = "reserve_participants", nullable = false)
    private Integer reserveParticipants;

    @Column(name = "current_participants", nullable = false)
    private Integer currentMainParticipants;

    @Column(name = "current_reserve_participants", nullable = false)
    private Integer currentReserveParticipants;

    @Column(name = "auto_approve_enabled", nullable = false)
    private Boolean autoApproveEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JointPurchaseOfferStatus status;

    @Column(name = "planned_start_at", nullable = false)
    private OffsetDateTime plannedStartAt;

    @Column(name = "planned_end_at", nullable = false)
    private OffsetDateTime plannedEndAt;

    @Column(name = "screenshot_bucket")
    private String screenshotBucket;

    @Column(name = "screenshot_object_key", length = 1024)
    private String screenshotObjectKey;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
