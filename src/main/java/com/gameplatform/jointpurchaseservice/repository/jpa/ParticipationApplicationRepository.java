package com.gameplatform.jointpurchaseservice.repository.jpa;

import com.gameplatform.jointpurchaseservice.domain.entity.ParticipationApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gameplatform.jointpurchaseservice.domain.enums.ParticipationApplicationStatus;

public interface ParticipationApplicationRepository extends JpaRepository<ParticipationApplication, UUID> {

    Optional<ParticipationApplication> findByOfferIdAndApplicantUserId(UUID offerId, UUID applicantUserId);

    boolean existsByOfferIdAndApplicantUserId(UUID offerId, UUID applicantUserId);

    List<ParticipationApplication> findAllByOfferIdOrderByCreatedAtAsc(UUID offerId);

    List<ParticipationApplication> findAllByApplicantUserIdAndStatusIn(UUID applicantUserId, List<ParticipationApplicationStatus> statuses);
}
