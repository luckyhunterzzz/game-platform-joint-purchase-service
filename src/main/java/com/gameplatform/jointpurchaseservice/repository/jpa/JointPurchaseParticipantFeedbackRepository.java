package com.gameplatform.jointpurchaseservice.repository.jpa;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseParticipantFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JointPurchaseParticipantFeedbackRepository extends JpaRepository<JointPurchaseParticipantFeedback, UUID> {

    Optional<JointPurchaseParticipantFeedback> findByOfferIdAndApplicationId(UUID offerId, UUID applicationId);

    List<JointPurchaseParticipantFeedback> findAllByOfferIdOrderByCreatedAtAsc(UUID offerId);

    long countByOfferIdAndApplicationIdIn(UUID offerId, List<UUID> applicationIds);
}
