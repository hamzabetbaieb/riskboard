package com.cacib.riskboard.service;

import com.cacib.riskboard.domain.RiskStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class RiskComputationService {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal GREEN_THRESHOLD = BigDecimal.valueOf(70);
    private static final BigDecimal ORANGE_THRESHOLD = BigDecimal.valueOf(90);

    public BigDecimal usageRate(BigDecimal usedAmount, BigDecimal maxAmount) {
        if (usedAmount == null || maxAmount == null || maxAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return usedAmount
                .multiply(ONE_HUNDRED)
                .divide(maxAmount, 2, RoundingMode.HALF_UP);
    }

    public RiskStatus riskStatus(BigDecimal usageRate) {
        if (usageRate.compareTo(GREEN_THRESHOLD) < 0) {
            return RiskStatus.GREEN;
        }
        if (usageRate.compareTo(ORANGE_THRESHOLD) <= 0) {
            return RiskStatus.ORANGE;
        }
        return RiskStatus.RED;
    }
}
