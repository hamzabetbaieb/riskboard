package com.cacib.riskboard.service;

import com.cacib.riskboard.dto.CounterpartyOptionDto;
import com.cacib.riskboard.repository.CounterpartyRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CounterpartyService {
    private final CounterpartyRepository counterpartyRepository;

    public CounterpartyService(CounterpartyRepository counterpartyRepository) {
        this.counterpartyRepository = counterpartyRepository;
    }

    public List<CounterpartyOptionDto> listOptions() {
        return counterpartyRepository.findAllByOrderByNameAsc().stream()
                .map(counterparty -> new CounterpartyOptionDto(counterparty.getId(), counterparty.getName()))
                .toList();
    }
}
