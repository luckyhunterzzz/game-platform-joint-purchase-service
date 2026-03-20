package com.gameplatform.jointpurchaseservice.repository.jpa;

import com.gameplatform.jointpurchaseservice.domain.entity.ParticipationApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ParticipationApplicationRepository extends JpaRepository<ParticipationApplication, UUID> {

    Optional<ParticipationApplication> findByOfferIdAndApplicantUserId(UUID offerId, UUID applicantUserId);

    boolean existsByOfferIdAndApplicantUserId(UUID offerId, UUID applicantUserId);
}