package com.cacib.riskboard.repository;

import com.cacib.riskboard.domain.DerogationRequest;
import com.cacib.riskboard.domain.DerogationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DerogationRequestRepository extends JpaRepository<DerogationRequest, Long> {
    List<DerogationRequest> findByStatusOrderByCreatedAtAsc(DerogationStatus status);
}
