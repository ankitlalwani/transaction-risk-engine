package com.transactionriskengine.aiexplanation.messaging.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RiskEvaluatedEvent(
        UUID eventId,
        String eventType,
        String eventVersion,
        Instant occurredAt,

        UUID riskDecisionId,

        UUID transactionId,
        String transactionReference,

        UUID customerId,
        UUID accountId,

        Integer riskScore,
        String riskLevel,
        String decisionStatus,

        String decisionReason,
        List<TriggeredRule> triggeredRules,

        Instant evaluatedAt
) {
}