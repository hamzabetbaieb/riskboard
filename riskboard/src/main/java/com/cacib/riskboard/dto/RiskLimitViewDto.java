package com.cacib.riskboard.dto;

import com.cacib.riskboard.domain.LimitType;
import com.cacib.riskboard.domain.RiskStatus;
import java.math.BigDecimal;

public record RiskLimitViewDto(
        Long riskLimitId,
        Long counterpartyId,
        String counterpartyName,
        String sector,
        LimitType limitType,
        BigDecimal maxAmount,
        BigDecimal usedAmount,
        BigDecimal usageRate,
        RiskStatus riskStatus,
        String currency
) {
}
