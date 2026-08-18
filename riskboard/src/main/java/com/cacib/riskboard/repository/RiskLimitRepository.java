package com.cacib.riskboard.repository;

import com.cacib.riskboard.domain.Counterparty;
import com.cacib.riskboard.domain.LimitType;
import com.cacib.riskboard.domain.RiskLimit;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskLimitRepository extends JpaRepository<RiskLimit, Long> {
    Optional<RiskLimit> findByCounterpartyAndLimitType(Counterparty counterparty, LimitType limitType);
}
