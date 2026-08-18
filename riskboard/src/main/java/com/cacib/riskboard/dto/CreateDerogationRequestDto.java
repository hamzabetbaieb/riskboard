package com.cacib.riskboard.dto;

import com.cacib.riskboard.domain.LimitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateDerogationRequestDto(
        @NotNull Long counterpartyId,
        @NotNull LimitType limitType,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount,
        @NotBlank @Size(min = 20) String reason,
        @NotBlank @Size(min = 6) String requestedBy
) {
}
