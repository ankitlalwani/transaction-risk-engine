package com.transactionriskengine.aiexplanation.explanation.application;

import com.transactionriskengine.aiexplanation.explanation.domain.AiExplanation;
import com.transactionriskengine.aiexplanation.explanation.repository.AiExplanationRepository;
import com.transactionriskengine.aiexplanation.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiExplanationQueryService {

    private final AiExplanationRepository aiExplanationRepository;

    public AiExplanation getByTransactionId(UUID transactionId) {
        return aiExplanationRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AI explanation not found for transactionId: " + transactionId
                ));
    }

    public AiExplanation getByRiskDecisionId(UUID riskDecisionId) {
        return aiExplanationRepository.findByRiskDecisionId(riskDecisionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AI explanation not found for riskDecisionId: " + riskDecisionId
                ));
    }
}