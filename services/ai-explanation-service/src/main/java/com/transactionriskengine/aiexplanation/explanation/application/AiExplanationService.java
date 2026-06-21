package com.transactionriskengine.aiexplanation.explanation.application;

import com.transactionriskengine.aiexplanation.explanation.domain.AiExplanation;
import com.transactionriskengine.aiexplanation.explanation.domain.ExplanationStatus;
import com.transactionriskengine.aiexplanation.explanation.domain.RiskLevel;
import com.transactionriskengine.aiexplanation.explanation.repository.AiExplanationRepository;
import com.transactionriskengine.aiexplanation.messaging.event.RiskEvaluatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiExplanationService {

    private final AiExplanationRepository aiExplanationRepository;
    private final ExplanationGenerator explanationGenerator;

    @Transactional
    public void generateExplanation(RiskEvaluatedEvent event) {
        if (aiExplanationRepository.existsByTransactionId(event.transactionId())) {
            log.info("AI explanation already exists for transactionId={}", event.transactionId());
            return;
        }

        GeneratedExplanation generated = explanationGenerator.generate(event);

        Instant now = Instant.now();

        AiExplanation explanation = AiExplanation.builder()
                .riskDecisionId(event.riskDecisionId())
                .transactionId(event.transactionId())
                .transactionReference(event.transactionReference())
                .customerId(event.customerId())
                .accountId(event.accountId())
                .riskScore(event.riskScore())
                .riskLevel(RiskLevel.valueOf(event.riskLevel()))
                .decisionStatus(event.decisionStatus())
                .explanationStatus(ExplanationStatus.GENERATED)
                .explanationText(generated.explanationText())
                .recommendedAction(generated.recommendedAction())
                .analystSummary(generated.analystSummary())
                .sourceEventId(event.eventId())
                .promptVersion(generated.promptVersion())
                .modelProvider(generated.modelProvider())
                .modelName(generated.modelName())
                .createdAt(now)
                .updatedAt(now)
                .build();

        aiExplanationRepository.save(explanation);

        log.info(
                "Generated AI explanation for transactionId={}, riskLevel={}",
                event.transactionId(),
                event.riskLevel()
        );
    }
}