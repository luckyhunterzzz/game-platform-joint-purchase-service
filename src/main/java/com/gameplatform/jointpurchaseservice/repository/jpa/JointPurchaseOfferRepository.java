package com.gameplatform.jointpurchaseservice.repository.jpa;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseOffer;
import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JointPurchaseOfferRepository extends JpaRepository<JointPurchaseOffer, UUID> {

    List<JointPurchaseOffer> findAllByOrganizerUserIdOrderByCreatedAtDesc(UUID organizerUserId);

    List<JointPurchaseOffer> findAllByStatusOrderByCreatedAtDesc(JointPurchaseOfferStatus status);

    List<JointPurchaseOffer> findAllByStatusInOrderByCreatedAtDesc(List<JointPurchaseOfferStatus> statuses);

    List<JointPurchaseOffer> findAllByIdInOrderByCreatedAtDesc(List<UUID> ids);

    boolean existsByOrganizerUserIdAndStatusIn(UUID organizerUserId, List<JointPurchaseOfferStatus> statuses);
}
