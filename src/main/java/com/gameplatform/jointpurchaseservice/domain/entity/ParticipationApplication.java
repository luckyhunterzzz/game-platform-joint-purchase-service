package com.gameplatform.jointpurchaseservice.domain.entity;

import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "participation_applications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationApplication {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "offer_id", nullable = false)
    private UUID offerId;

    @Column(name = "applicant_user_id", nullable = false)
    private UUID applicantUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ParticipationApplicationStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}