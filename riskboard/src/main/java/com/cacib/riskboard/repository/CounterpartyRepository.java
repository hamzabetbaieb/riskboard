package com.cacib.riskboard.repository;

import com.cacib.riskboard.domain.Counterparty;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterpartyRepository extends JpaRepository<Counterparty, Long> {
    Optional<Counterparty> findByRicosCode(String ricosCode);

    List<Counterparty> findAllByOrderByNameAsc();
}
