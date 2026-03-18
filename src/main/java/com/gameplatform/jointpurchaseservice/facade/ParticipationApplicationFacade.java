package com.gameplatform.jointpurchaseservice.facade;

import com.gameplatform.jointpurchaseservice.domain.entity.ParticipationApplication;
import com.gameplatform.jointpurchaseservice.dto.response.ParticipationApplicationResponseDto;
import com.gameplatform.jointpurchaseservice.mapper.ParticipationApplicationMapper;
import com.gameplatform.jointpurchaseservice.service.ParticipationApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ParticipationApplicationFacade {

    private final ParticipationApplicationService participationApplicationService;
    private final ParticipationApplicationMapper participationApplicationMapper;

    public ParticipationApplicationResponseDto submitApplication(UUID offerId, UUID applicantUserId) {
        ParticipationApplication application =
                participationApplicationService.submitApplication(offerId, applicantUserId);

        return participationApplicationMapper.toResponseDto(application);
    }
}