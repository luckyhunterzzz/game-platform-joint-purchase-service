package com.gameplatform.jointpurchaseservice.repository.jpa;

import com.gameplatform.jointpurchaseservice.domain.entity.JointPurchaseOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JointPurchaseOfferRepository extends JpaRepository<JointPurchaseOffer, UUID> {
}