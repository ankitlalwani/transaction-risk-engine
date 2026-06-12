package com.transactionriskengine.transactioningestion.messaging.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop20ByEventStatusOrderByCreatedAtAsc(OutboxEventStatus eventStatus);
}
