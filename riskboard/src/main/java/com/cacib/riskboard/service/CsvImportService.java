package com.cacib.riskboard.service;

import com.cacib.riskboard.domain.Counterparty;
import com.cacib.riskboard.domain.LimitType;
import com.cacib.riskboard.domain.RiskLimit;
import com.cacib.riskboard.dto.CsvImportErrorDto;
import com.cacib.riskboard.dto.CsvImportSummaryDto;
import com.cacib.riskboard.repository.CounterpartyRepository;
import com.cacib.riskboard.repository.RiskLimitRepository;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CsvImportService {
    private final CounterpartyRepository counterpartyRepository;
    private final RiskLimitRepository riskLimitRepository;

    public CsvImportService(CounterpartyRepository counterpartyRepository, RiskLimitRepository riskLimitRepository) {
        this.counterpartyRepository = counterpartyRepository;
        this.riskLimitRepository = riskLimitRepository;
    }

    public CsvImportSummaryDto importRiskLimits(MultipartFile file) {
        List<CsvImportErrorDto> errors = new ArrayList<>();
        int successCount = 0;
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        try (CSVParser parser = new CSVParser(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8), format
        )) {
            for (CSVRecord record : parser) {
                try {
                    upsertRecord(record);
                    successCount++;
                } catch (RuntimeException exception) {
                    errors.add(new CsvImportErrorDto(
                            (int) record.getRecordNumber() + 1,
                            record.toString(),
                            exception.getMessage()
                    ));
                }
            }
        } catch (IOException exception) {
            errors.add(new CsvImportErrorDto(0, "", "Cannot read CSV file: " + exception.getMessage()));
        }

        return new CsvImportSummaryDto(successCount, errors.size(), errors);
    }

    private void upsertRecord(CSVRecord record) {
        if (record.size() != 8) {
            throw new IllegalArgumentException("Expected 8 columns but found " + record.size());
        }

        String name = normalize(record.get(0));
        String ricosCode = normalize(record.get(1));
        String country = normalize(record.get(2));
        String sector = normalize(record.get(3));
        LimitType limitType = LimitType.valueOf(normalize(record.get(4)).toUpperCase(Locale.ROOT));
        BigDecimal maxAmount = new BigDecimal(normalize(record.get(5)));
        BigDecimal usedAmount = new BigDecimal(normalize(record.get(6)));
        String currency = normalize(record.get(7));

        if (maxAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("maxAmount must be strictly positive");
        }
        if (usedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("usedAmount must be positive or zero");
        }

        Counterparty counterparty = counterpartyRepository.findByRicosCode(ricosCode).orElseGet(Counterparty::new);
        counterparty.setName(name);
        counterparty.setRicosCode(ricosCode);
        counterparty.setCountry(country);
        counterparty.setSector(sector);
        Counterparty savedCounterparty = counterpartyRepository.save(counterparty);

        RiskLimit riskLimit = riskLimitRepository.findByCounterpartyAndLimitType(savedCounterparty, limitType)
                .orElseGet(RiskLimit::new);
        riskLimit.setCounterparty(savedCounterparty);
        riskLimit.setLimitType(limitType);
        riskLimit.setMaxAmount(maxAmount);
        riskLimit.setUsedAmount(usedAmount);
        riskLimit.setCurrency(currency);
        riskLimit.setLastUpdated(LocalDateTime.now());
        riskLimitRepository.save(riskLimit);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
