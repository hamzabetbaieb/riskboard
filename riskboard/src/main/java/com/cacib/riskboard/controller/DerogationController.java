package com.cacib.riskboard.controller;

import com.cacib.riskboard.domain.LimitType;
import com.cacib.riskboard.dto.AmountValidationResponseDto;
import com.cacib.riskboard.dto.CreateDerogationRequestDto;
import com.cacib.riskboard.dto.DerogationResponseDto;
import com.cacib.riskboard.service.DerogationService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/derogations")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class DerogationController {
    private final DerogationService derogationService;

    public DerogationController(DerogationService derogationService) {
        this.derogationService = derogationService;
    }

    @PostMapping
    public DerogationResponseDto create(@Valid @RequestBody CreateDerogationRequestDto request) {
        return derogationService.create(request);
    }

    @GetMapping("/pending")
    public List<DerogationResponseDto> listPending() {
        return derogationService.listPending();
    }

    @PostMapping("/{id}/approve")
    public DerogationResponseDto approve(@PathVariable Long id) {
        return derogationService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public DerogationResponseDto reject(@PathVariable Long id) {
        return derogationService.reject(id);
    }

    @GetMapping("/validate-amount")
    public AmountValidationResponseDto validateAmount(
            @RequestParam(required = false) Long counterpartyId,
            @RequestParam(required = false) LimitType limitType,
            @RequestParam(required = false) BigDecimal amount
    ) {
        return derogationService.validateAmount(counterpartyId, limitType, amount);
    }
}
