package com.gameplatform.jointpurchaseservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JointPurchaseOfferLifecycleScheduler {

    private final JointPurchaseOfferService jointPurchaseOfferService;

    @Scheduled(fixedDelayString = "${app.joint-purchase.auto-cancel-check-delay-ms:300000}")
    public void autoCancelExpiredOffers() {
        jointPurchaseOfferService.autoCancelExpiredOffers();
    }
}
