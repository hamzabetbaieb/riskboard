package com.cacib.riskboard.dto;

import java.math.BigDecimal;

public record AmountValidationResponseDto(boolean valid, String message, BigDecimal maxAllowedAmount) {
}
