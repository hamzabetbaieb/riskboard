package com.cacib.riskboard.dto;

import com.cacib.riskboard.domain.DerogationStatus;
import com.cacib.riskboard.domain.LimitType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DerogationResponseDto(
        Long id,
        Long counterpartyId,
        String counterpartyName,
        LimitType limitType,
        String requestedBy,
        BigDecimal amount,
        String reason,
        DerogationStatus status,
        LocalDateTime createdAt
) {
}
