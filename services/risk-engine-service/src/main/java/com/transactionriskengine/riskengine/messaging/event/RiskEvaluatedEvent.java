package com.transactionriskengine.riskengine.messaging.event;

import com.transactionriskengine.riskengine.risk.domain.DecisionStatus;
import com.transactionriskengine.riskengine.risk.domain.RiskLevel;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RiskEvaluatedEvent(
        UUID eventId,
        UUID riskDecisionId,
        UUID transactionId,
        String transactionReference,
        UUID customerId,
        UUID accountId,
        int riskScore,
        RiskLevel riskLevel,
        DecisionStatus decisionStatus,
        String decisionReason,
        List<TriggeredRule> triggeredRules,
        Instant evaluatedAt
) {
}
