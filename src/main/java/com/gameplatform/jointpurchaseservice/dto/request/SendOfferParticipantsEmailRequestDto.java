package com.gameplatform.jointpurchaseservice.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class SendOfferParticipantsEmailRequestDto {

    @NotBlank(message = "must not be blank")
    @Size(max = 255, message = "must not exceed 255 characters")
    private String subject;

    @NotBlank(message = "must not be blank")
    @Size(max = 5000, message = "must not exceed 5000 characters")
    private String message;

    private Boolean sendToMain;

    private Boolean sendToReserve;
}
