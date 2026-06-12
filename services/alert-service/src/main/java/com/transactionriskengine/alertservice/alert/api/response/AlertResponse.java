package com.transactionriskengine.alertservice.alert.api.response;

import com.transactionriskengine.alertservice.alert.domain.AlertPriority;
import com.transactionriskengine.alertservice.alert.domain.AlertStatus;
import com.transactionriskengine.alertservice.alert.domain.DecisionStatus;
import com.transactionriskengine.alertservice.alert.domain.RiskLevel;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        String alertReference,
        UUID transactionId,
        String transactionReference,
        UUID riskDecisionId,
        UUID customerId,
        UUID accountId,
        Integer riskScore,
        RiskLevel riskLevel,
        DecisionStatus decisionStatus,
        AlertStatus alertStatus,
        AlertPriority alertPriority,
        String alertReason,
        String triggeredRules,
        String assignedTo,
        Instant createdAt,
        Instant updatedAt,
        Instant closedAt
) {
}
