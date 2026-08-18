package com.cacib.riskboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cacib.riskboard.domain.Counterparty;
import com.cacib.riskboard.domain.RiskLimit;
import com.cacib.riskboard.dto.CsvImportSummaryDto;
import com.cacib.riskboard.repository.CounterpartyRepository;
import com.cacib.riskboard.repository.RiskLimitRepository;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

class CsvImportServiceTest {
    private final CounterpartyRepository counterpartyRepository = Mockito.mock(CounterpartyRepository.class);
    private final RiskLimitRepository riskLimitRepository = Mockito.mock(RiskLimitRepository.class);
    private final CsvImportService csvImportService = new CsvImportService(counterpartyRepository, riskLimitRepository);

    @Test
    void shouldImportQuotedFieldsContainingCommas() {
        String csv = """
                name,ricosCode,country,sector,limitType,maxAmount,usedAmount,currency
                "ACME, INC",RICOS123,FR,"Energy, Utilities",CREDIT,1000,250,EUR
                """;
        MockMultipartFile file = new MockMultipartFile(
                "file", "risk-limits.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );
        when(counterpartyRepository.findByRicosCode("RICOS123")).thenReturn(Optional.empty());
        when(counterpartyRepository.save(any(Counterparty.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(riskLimitRepository.findByCounterpartyAndLimitType(any(Counterparty.class), any())).thenReturn(Optional.empty());

        CsvImportSummaryDto summary = csvImportService.importRiskLimits(file);

        ArgumentCaptor<Counterparty> counterpartyCaptor = ArgumentCaptor.forClass(Counterparty.class);
        verify(counterpartyRepository).save(counterpartyCaptor.capture());
        verify(riskLimitRepository).save(any(RiskLimit.class));
        assertThat(summary.successfulRows()).isEqualTo(1);
        assertThat(summary.errorRows()).isZero();
        assertThat(counterpartyCaptor.getValue().getName()).isEqualTo("ACME, INC");
        assertThat(counterpartyCaptor.getValue().getSector()).isEqualTo("Energy, Utilities");
    }
}
