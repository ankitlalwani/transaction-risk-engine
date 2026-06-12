package com.transactionriskengine.riskengine.messaging.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RiskOutboxEventRepository extends JpaRepository<RiskOutboxEvent, UUID> {

    List<RiskOutboxEvent> findTop20ByEventStatusOrderByCreatedAtAsc(
            RiskOutboxEventStatus eventStatus
    );
}
