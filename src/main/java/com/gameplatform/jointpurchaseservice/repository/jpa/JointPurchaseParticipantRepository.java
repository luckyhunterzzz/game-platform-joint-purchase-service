package com.gameplatform.jointpurchaseservice.repository.jpa;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseParticipant;
import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseParticipantStatus;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JointPurchaseParticipantRepository extends JpaRepository<JointPurchaseParticipant, UUID> {

    boolean existsByUserIdAndParticipationTypeAndStatus(
            UUID userId,
            ParticipationType participationType,
            JointPurchaseParticipantStatus status
    );

    boolean existsByOfferIdAndUserIdAndStatus(UUID offerId, UUID userId, JointPurchaseParticipantStatus status);

    boolean existsByUserIdAndStatus(UUID userId, JointPurchaseParticipantStatus status);

    @Query(
            value = """
                    SELECT EXISTS (
                        SELECT 1
                        FROM joint_purchase_participants participant
                        JOIN joint_purchase_offers offer ON offer.id = participant.offer_id
                        WHERE participant.user_id = :userId
                          AND participant.status = 'ACTIVE'
                          AND offer.status IN (:activeOfferStatuses)
                    )
                    """,
            nativeQuery = true
    )
    boolean existsByUserIdInActiveOffers(
            @Param("userId") UUID userId,
            @Param("activeOfferStatuses") List<String> activeOfferStatuses
    );

    @Query(
            value = """
                    SELECT EXISTS (
                        SELECT 1
                        FROM joint_purchase_participants participant
                        JOIN joint_purchase_offers offer ON offer.id = participant.offer_id
                        WHERE participant.user_id = :userId
                          AND participant.participation_type = 'MAIN'
                          AND participant.status = 'ACTIVE'
                          AND offer.status IN (:activeOfferStatuses)
                    )
                    """,
            nativeQuery = true
    )
    boolean existsByUserIdInActiveMainOffers(
            @Param("userId") UUID userId,
            @Param("activeOfferStatuses") List<String> activeOfferStatuses
    );

    long countByOfferIdAndParticipationTypeAndStatus(
            UUID offerId,
            ParticipationType participationType,
            JointPurchaseParticipantStatus status
    );

    List<JointPurchaseParticipant> findAllByOfferIdAndStatusOrderByJoinedAtAsc(
            UUID offerId,
            JointPurchaseParticipantStatus status
    );

    List<JointPurchaseParticipant> findAllByOfferIdAndParticipationTypeAndStatus(
            UUID offerId,
            ParticipationType participationType,
            JointPurchaseParticipantStatus status
    );

    List<JointPurchaseParticipant> findAllByUserIdAndParticipationTypeAndStatus(
            UUID userId,
            ParticipationType participationType,
            JointPurchaseParticipantStatus status
    );

    Optional<JointPurchaseParticipant> findByApplicationIdAndStatus(UUID applicationId, JointPurchaseParticipantStatus status);

    Optional<JointPurchaseParticipant> findByOfferIdAndUserIdAndParticipationTypeAndStatus(
            UUID offerId,
            UUID userId,
            ParticipationType participationType,
            JointPurchaseParticipantStatus status
    );
}
