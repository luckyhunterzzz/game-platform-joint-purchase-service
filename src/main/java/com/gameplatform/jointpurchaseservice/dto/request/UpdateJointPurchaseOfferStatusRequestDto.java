package com.gameplatform.jointpurchaseservice.dto.request;

import com.gameplatform.jointpurchaseservice.domain.enums.JointPurchaseOfferStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJointPurchaseOfferStatusRequestDto {

    @NotNull
    private JointPurchaseOfferStatus status;
}
