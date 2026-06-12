package com.transactionriskengine.riskengine.risk.api;

import com.transactionriskengine.riskengine.risk.domain.DecisionStatus;
import com.transactionriskengine.riskengine.risk.domain.RiskDecision;
import com.transactionriskengine.riskengine.risk.domain.RiskLevel;
import com.transactionriskengine.riskengine.risk.domain.TriggeredRule;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RiskDecisionResponse(
        UUID transactionId,
        String transactionReference,
        int riskScore,
        RiskLevel riskLevel,
        DecisionStatus decisionStatus,
        String decisionReason,
        List<TriggeredRule> triggeredRules,
        Instant evaluatedAt
) {

    public static RiskDecisionResponse from(RiskDecision decision) {
        return new RiskDecisionResponse(
                decision.getTransactionId(),
                decision.getTransactionReference(),
                decision.getRiskScore(),
                decision.getRiskLevel(),
                decision.getDecisionStatus(),
                decision.getDecisionReason(),
                List.copyOf(decision.getTriggeredRules()),
                decision.getEvaluatedAt()
        );
    }
}
