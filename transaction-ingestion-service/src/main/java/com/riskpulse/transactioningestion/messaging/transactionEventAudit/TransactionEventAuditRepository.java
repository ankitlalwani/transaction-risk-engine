package com.riskpulse.transactioningestion.messaging.transactionEventAudit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionEventAuditRepository extends JpaRepository<TransactionEvent, UUID> {
}
