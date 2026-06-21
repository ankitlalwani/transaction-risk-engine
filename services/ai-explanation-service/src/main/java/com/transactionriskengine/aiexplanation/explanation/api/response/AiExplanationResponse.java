package com.transactionriskengine.aiexplanation.explanation.api.response;

import com.transactionriskengine.aiexplanation.explanation.domain.ExplanationStatus;
import com.transactionriskengine.aiexplanation.explanation.domain.RiskLevel;

import java.time.Instant;
import java.util.UUID;

public record AiExplanationResponse(
        UUID id,
        UUID riskDecisionId,
        UUID transactionId,
        String transactionReference,
        UUID customerId,
        UUID accountId,
        Integer riskScore,
        RiskLevel riskLevel,
        String decisionStatus,
        ExplanationStatus explanationStatus,
        String explanationText,
        String recommendedAction,
        String analystSummary,
        String modelProvider,
        String modelName,
        String promptVersion,
        Instant createdAt,
        Instant updatedAt
) {
}