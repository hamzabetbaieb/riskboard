package com.cacib.riskboard.service;

import com.cacib.riskboard.domain.Counterparty;
import com.cacib.riskboard.domain.DerogationRequest;
import com.cacib.riskboard.domain.DerogationStatus;
import com.cacib.riskboard.domain.LimitType;
import com.cacib.riskboard.domain.RiskLimit;
import com.cacib.riskboard.dto.AmountValidationResponseDto;
import com.cacib.riskboard.dto.CreateDerogationRequestDto;
import com.cacib.riskboard.dto.DerogationResponseDto;
import com.cacib.riskboard.exception.BusinessValidationException;
import com.cacib.riskboard.repository.CounterpartyRepository;
import com.cacib.riskboard.repository.DerogationRequestRepository;
import com.cacib.riskboard.repository.RiskLimitRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DerogationService {
    private final CounterpartyRepository counterpartyRepository;
    private final RiskLimitRepository riskLimitRepository;
    private final DerogationRequestRepository derogationRequestRepository;

    public DerogationService(
            CounterpartyRepository counterpartyRepository,
            RiskLimitRepository riskLimitRepository,
            DerogationRequestRepository derogationRequestRepository
    ) {
        this.counterpartyRepository = counterpartyRepository;
        this.riskLimitRepository = riskLimitRepository;
        this.derogationRequestRepository = derogationRequestRepository;
    }

    @Transactional
    public DerogationResponseDto create(CreateDerogationRequestDto request) {
        Counterparty counterparty = counterpartyRepository.findById(request.counterpartyId())
                .orElseThrow(() -> new BusinessValidationException("Counterparty not found"));
        RiskLimit riskLimit = riskLimitRepository.findByCounterpartyAndLimitType(counterparty, request.limitType())
                .orElseThrow(() -> new BusinessValidationException("No risk limit found for selected counterparty and risk type"));

        BigDecimal maxAllowed = maxAllowedAmount(riskLimit.getMaxAmount());
        if (request.amount().compareTo(maxAllowed) > 0) {
            throw new BusinessValidationException("Requested amount exceeds 150% of maximum limit");
        }

        DerogationRequest derogation = new DerogationRequest();
        derogation.setCounterparty(counterparty);
        derogation.setLimitType(request.limitType());
        derogation.setAmount(request.amount());
        derogation.setReason(request.reason());
        derogation.setRequestedBy(request.requestedBy());
        derogation.setStatus(DerogationStatus.PENDING);
        derogation.setCreatedAt(LocalDateTime.now());

        return toDto(derogationRequestRepository.save(derogation));
    }

    @Transactional(readOnly = true)
    public List<DerogationResponseDto> listPending() {
        return derogationRequestRepository.findByStatusOrderByCreatedAtAsc(DerogationStatus.PENDING)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public DerogationResponseDto approve(Long id) {
        return changeStatus(id, DerogationStatus.APPROVED);
    }

    @Transactional
    public DerogationResponseDto reject(Long id) {
        return changeStatus(id, DerogationStatus.REJECTED);
    }

    @Transactional(readOnly = true)
    public AmountValidationResponseDto validateAmount(Long counterpartyId, LimitType limitType, BigDecimal amount) {
        if (counterpartyId == null || limitType == null || amount == null) {
            return new AmountValidationResponseDto(false, "counterpartyId, limitType and amount are required", null);
        }

        Counterparty counterparty = counterpartyRepository.findById(counterpartyId).orElse(null);
        if (counterparty == null) {
            return new AmountValidationResponseDto(false, "Counterparty not found", null);
        }

        RiskLimit riskLimit = riskLimitRepository.findByCounterpartyAndLimitType(counterparty, limitType).orElse(null);
        if (riskLimit == null) {
            return new AmountValidationResponseDto(false, "No risk limit for selected counterparty and risk type", null);
        }

        BigDecimal maxAllowed = maxAllowedAmount(riskLimit.getMaxAmount());
        if (amount.compareTo(maxAllowed) > 0) {
            return new AmountValidationResponseDto(false, "Amount exceeds 150% of max amount", maxAllowed);
        }

        return new AmountValidationResponseDto(true, "Amount is valid", maxAllowed);
    }

    private DerogationResponseDto changeStatus(Long id, DerogationStatus targetStatus) {
        DerogationRequest request = derogationRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessValidationException("Derogation request not found"));
        request.setStatus(targetStatus);
        return toDto(derogationRequestRepository.save(request));
    }

    private BigDecimal maxAllowedAmount(BigDecimal maxAmount) {
        return maxAmount.multiply(BigDecimal.valueOf(1.5));
    }

    private DerogationResponseDto toDto(DerogationRequest request) {
        return new DerogationResponseDto(
                request.getId(),
                request.getCounterparty().getId(),
                request.getCounterparty().getName(),
                request.getLimitType(),
                request.getRequestedBy(),
                request.getAmount(),
                request.getReason(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
