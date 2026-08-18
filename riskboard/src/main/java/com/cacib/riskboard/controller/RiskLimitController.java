package com.cacib.riskboard.controller;

import com.cacib.riskboard.domain.LimitType;
import com.cacib.riskboard.dto.CsvImportSummaryDto;
import com.cacib.riskboard.dto.RiskLimitViewDto;
import com.cacib.riskboard.dto.SectorExposureDto;
import com.cacib.riskboard.service.CsvImportService;
import com.cacib.riskboard.service.RiskLimitService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/risk-limits")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class RiskLimitController {
    private final CsvImportService csvImportService;
    private final RiskLimitService riskLimitService;

    public RiskLimitController(CsvImportService csvImportService, RiskLimitService riskLimitService) {
        this.csvImportService = csvImportService;
        this.riskLimitService = riskLimitService;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CsvImportSummaryDto importCsv(@RequestParam("file") MultipartFile file) {
        return csvImportService.importRiskLimits(file);
    }

    @GetMapping
    public List<RiskLimitViewDto> listDetailed() {
        return riskLimitService.listDetailed();
    }

    @GetMapping("/aggregated")
    public List<SectorExposureDto> sectorExposureByType(@RequestParam LimitType limitType) {
        return riskLimitService.sectorExposureByType(limitType);
    }

    @GetMapping("/aggregated-map")
    public Map<String, BigDecimal> sectorExposureMap(@RequestParam LimitType limitType) {
        return riskLimitService.exposureBySectorMap(limitType);
    }
}
