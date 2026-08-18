package com.cacib.riskboard.controller;

import com.cacib.riskboard.dto.CounterpartyOptionDto;
import com.cacib.riskboard.service.CounterpartyService;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/counterparties")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class CounterpartyController {
    private final CounterpartyService counterpartyService;

    public CounterpartyController(CounterpartyService counterpartyService) {
        this.counterpartyService = counterpartyService;
    }

    @GetMapping
    public List<CounterpartyOptionDto> listCounterparties() {
        return counterpartyService.listOptions();
    }
}
