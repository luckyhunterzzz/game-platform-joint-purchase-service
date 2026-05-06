package com.gameplatform.jointpurchaseservice.repository.jpa;

import com.gameplatform.jointpurchaseservice.domain.entity.ParticipationApplication;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticipationApplicationRepository extends JpaRepository<ParticipationApplication, UUID> {

    Optional<ParticipationApplication> findByOfferIdAndApplicantUserId(UUID offerId, UUID applicantUserId);

    Optional<ParticipationApplication> findFirstByOfferIdAndApplicantUserIdAndStatusInOrderByUpdatedAtDesc(
            UUID offerId,
            UUID applicantUserId,
            List<ParticipationApplicationStatus> statuses
    );

    boolean existsByOfferIdAndApplicantUserId(UUID offerId, UUID applicantUserId);

    boolean existsByOfferIdAndApplicantUserIdAndStatusIn(
            UUID offerId,
            UUID applicantUserId,
            List<ParticipationApplicationStatus> statuses
    );

    List<ParticipationApplication> findAllByOfferIdOrderByCreatedAtAsc(UUID offerId);

    List<ParticipationApplication> findAllByApplicantUserIdAndStatusIn(UUID applicantUserId, List<ParticipationApplicationStatus> statuses);
}
