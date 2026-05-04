package com.gameplatform.jointpurchaseservice.exception;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message
) {
}
