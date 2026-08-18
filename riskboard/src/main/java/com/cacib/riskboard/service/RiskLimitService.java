package com.cacib.riskboard.service;

import com.cacib.riskboard.domain.LimitType;
import com.cacib.riskboard.domain.RiskLimit;
import com.cacib.riskboard.dto.RiskLimitViewDto;
import com.cacib.riskboard.dto.SectorExposureDto;
import com.cacib.riskboard.repository.RiskLimitRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiskLimitService {
    private final RiskLimitRepository riskLimitRepository;
    private final RiskComputationService riskComputationService;

    public RiskLimitService(RiskLimitRepository riskLimitRepository, RiskComputationService riskComputationService) {
        this.riskLimitRepository = riskLimitRepository;
        this.riskComputationService = riskComputationService;
    }

    @Transactional(readOnly = true)
    public List<RiskLimitViewDto> listDetailed() {
        List<RiskLimitViewDto> views = new ArrayList<>();
        for (RiskLimit riskLimit : riskLimitRepository.findAll()) {
            BigDecimal usageRate = riskComputationService.usageRate(riskLimit.getUsedAmount(), riskLimit.getMaxAmount());
            views.add(new RiskLimitViewDto(
                    riskLimit.getId(),
                    riskLimit.getCounterparty().getId(),
                    riskLimit.getCounterparty().getName(),
                    riskLimit.getCounterparty().getSector(),
                    riskLimit.getLimitType(),
                    riskLimit.getMaxAmount(),
                    riskLimit.getUsedAmount(),
                    usageRate,
                    riskComputationService.riskStatus(usageRate),
                    riskLimit.getCurrency()
            ));
        }

        views.sort(Comparator
                .comparing(RiskLimitViewDto::counterpartyName)
                .thenComparing(dto -> dto.limitType().name())
                .thenComparing(RiskLimitViewDto::sector)
                .thenComparing(RiskLimitViewDto::maxAmount, Comparator.reverseOrder())
                .thenComparing(RiskLimitViewDto::usedAmount, Comparator.reverseOrder())
                .thenComparing(RiskLimitViewDto::usageRate, Comparator.reverseOrder())
                .thenComparing(dto -> dto.riskStatus().name()));
        return views;
    }

    @Transactional(readOnly = true)
    public List<SectorExposureDto> sectorExposureByType(LimitType limitType) {
        Map<String, BigDecimal> exposureBySector = riskLimitRepository.findAll().stream()
                .filter(riskLimit -> riskLimit.getLimitType() == limitType)
                .collect(Collectors.groupingBy(
                        riskLimit -> riskLimit.getCounterparty().getSector(),
                        Collectors.mapping(RiskLimit::getUsedAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return exposureBySector.entrySet().stream()
                .map(entry -> new SectorExposureDto(limitType, entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(SectorExposureDto::sector))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> exposureBySectorMap(LimitType limitType) {
        return sectorExposureByType(limitType).stream()
                .collect(Collectors.toMap(SectorExposureDto::sector, SectorExposureDto::totalUsedAmount));
    }
}
