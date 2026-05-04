package com.gameplatform.jointpurchaseservice.dto.request;

import com.gameplatform.jointpurchaseservice.domain.enums.ParticipantFeedbackResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class UpsertParticipantFeedbackRequestDto {

    @NotNull
    private ParticipantFeedbackResult result;

    @Size(max = 3000)
    private String description;
}
