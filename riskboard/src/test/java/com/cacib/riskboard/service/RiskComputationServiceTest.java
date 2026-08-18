package com.cacib.riskboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.cacib.riskboard.domain.RiskStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RiskComputationServiceTest {
    private final RiskComputationService riskComputationService = new RiskComputationService();

    @Test
    void shouldReturnGreenWhenUsageRateIsBelow70() {
        RiskStatus status = riskComputationService.riskStatus(BigDecimal.valueOf(69.99));
        assertThat(status).isEqualTo(RiskStatus.GREEN);
    }

    @Test
    void shouldReturnOrangeWhenUsageRateIsBetween70And90() {
        assertThat(riskComputationService.riskStatus(BigDecimal.valueOf(70))).isEqualTo(RiskStatus.ORANGE);
        assertThat(riskComputationService.riskStatus(BigDecimal.valueOf(90))).isEqualTo(RiskStatus.ORANGE);
    }

    @Test
    void shouldReturnRedWhenUsageRateIsAbove90() {
        RiskStatus status = riskComputationService.riskStatus(BigDecimal.valueOf(90.01));
        assertThat(status).isEqualTo(RiskStatus.RED);
    }

    @Test
    void shouldCalculateUsageRateAndReturnGreenBelow70Percent() {
        BigDecimal usageRate = riskComputationService.usageRate(BigDecimal.valueOf(69), BigDecimal.valueOf(100));

        assertThat(usageRate).isEqualByComparingTo("69.00");
        assertThat(riskComputationService.riskStatus(usageRate)).isEqualTo(RiskStatus.GREEN);
    }

    @Test
    void shouldCalculateUsageRateAndReturnOrangeBetween70And90Percent() {
        BigDecimal usageRate = riskComputationService.usageRate(BigDecimal.valueOf(80), BigDecimal.valueOf(100));

        assertThat(usageRate).isEqualByComparingTo("80.00");
        assertThat(riskComputationService.riskStatus(usageRate)).isEqualTo(RiskStatus.ORANGE);
    }

    @Test
    void shouldCalculateUsageRateAndReturnRedAbove90Percent() {
        BigDecimal usageRate = riskComputationService.usageRate(BigDecimal.valueOf(95), BigDecimal.valueOf(100));

        assertThat(usageRate).isEqualByComparingTo("95.00");
        assertThat(riskComputationService.riskStatus(usageRate)).isEqualTo(RiskStatus.RED);
    }
}
