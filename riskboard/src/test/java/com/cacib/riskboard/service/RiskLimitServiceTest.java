package com.cacib.riskboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.cacib.riskboard.domain.Counterparty;
import com.cacib.riskboard.domain.LimitType;
import com.cacib.riskboard.domain.RiskLimit;
import com.cacib.riskboard.repository.RiskLimitRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RiskLimitServiceTest {
    private final RiskLimitRepository riskLimitRepository = Mockito.mock(RiskLimitRepository.class);
    private final RiskLimitService riskLimitService = new RiskLimitService(riskLimitRepository, new RiskComputationService());

    @Test
    void shouldAggregateExposureBySector() {
        Counterparty banking = counterparty("Bank A", "RICOS1", "FR", "Banking");
        Counterparty energy = counterparty("Energy A", "RICOS2", "FR", "Energy");
        Counterparty banking2 = counterparty("Bank B", "RICOS3", "DE", "Banking");

        RiskLimit bankingCredit1 = riskLimit(banking, LimitType.CREDIT, "100", "40");
        RiskLimit energyCredit = riskLimit(energy, LimitType.CREDIT, "200", "60");
        RiskLimit bankingCredit2 = riskLimit(banking2, LimitType.CREDIT, "80", "30");
        RiskLimit bankingMarket = riskLimit(banking, LimitType.MARKET, "100", "20");

        when(riskLimitRepository.findAll()).thenReturn(List.of(bankingCredit1, energyCredit, bankingCredit2, bankingMarket));

        Map<String, BigDecimal> result = riskLimitService.exposureBySectorMap(LimitType.CREDIT);

        assertThat(result).containsEntry("Banking", BigDecimal.valueOf(70));
        assertThat(result).containsEntry("Energy", BigDecimal.valueOf(60));
        assertThat(result).hasSize(2);
    }

    private Counterparty counterparty(String name, String ricosCode, String country, String sector) {
        Counterparty counterparty = new Counterparty();
        counterparty.setName(name);
        counterparty.setRicosCode(ricosCode);
        counterparty.setCountry(country);
        counterparty.setSector(sector);
        return counterparty;
    }

    private RiskLimit riskLimit(Counterparty counterparty, LimitType limitType, String max, String used) {
        RiskLimit riskLimit = new RiskLimit();
        riskLimit.setCounterparty(counterparty);
        riskLimit.setLimitType(limitType);
        riskLimit.setMaxAmount(new BigDecimal(max));
        riskLimit.setUsedAmount(new BigDecimal(used));
        riskLimit.setCurrency("EUR");
        riskLimit.setLastUpdated(LocalDateTime.now());
        return riskLimit;
    }
}
