package com.transactionriskengine.aiexplanation.explanation.mapper;

import com.transactionriskengine.aiexplanation.explanation.api.response.AiExplanationResponse;
import com.transactionriskengine.aiexplanation.explanation.domain.AiExplanation;
import org.springframework.stereotype.Component;

@Component
public class AiExplanationMapper {

    public AiExplanationResponse toResponse(AiExplanation explanation) {
        return new AiExplanationResponse(
                explanation.getId(),
                explanation.getRiskDecisionId(),
                explanation.getTransactionId(),
                explanation.getTransactionReference(),
                explanation.getCustomerId(),
                explanation.getAccountId(),
                explanation.getRiskScore(),
                explanation.getRiskLevel(),
                explanation.getDecisionStatus(),
                explanation.getExplanationStatus(),
                explanation.getExplanationText(),
                explanation.getRecommendedAction(),
                explanation.getAnalystSummary(),
                explanation.getModelProvider(),
                explanation.getModelName(),
                explanation.getPromptVersion(),
                explanation.getCreatedAt(),
                explanation.getUpdatedAt()
        );
    }
}