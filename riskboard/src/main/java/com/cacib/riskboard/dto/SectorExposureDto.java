package com.cacib.riskboard.dto;

import com.cacib.riskboard.domain.LimitType;
import java.math.BigDecimal;

public record SectorExposureDto(LimitType limitType, String sector, BigDecimal totalUsedAmount) {
}
